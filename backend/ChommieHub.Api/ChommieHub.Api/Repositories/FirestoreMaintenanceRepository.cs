using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore implementation used to store and retrieve maintenance requests.
    /// </summary>
    public sealed class FirestoreMaintenanceRepository : IMaintenanceRepository
    {
        private const string CollectionName = "maintenanceRequests";

        private readonly FirestoreDb _firestore;

        public FirestoreMaintenanceRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<MaintenanceRequest> CreateAsync(
            MaintenanceRequest request,
            CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document();

            request.Id = documentReference.Id;

            await documentReference.SetAsync(
                request,
                cancellationToken: cancellationToken);

            return request;
        }

        public async Task<IReadOnlyList<MaintenanceRequest>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("ResidentId", residentId);

            QuerySnapshot snapshot =
                await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<MaintenanceRequest>())
                .OrderByDescending(request => request.CreatedAtUtc)
                .ToList();
        }

        public async Task<IReadOnlyList<MaintenanceRequest>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("AccommodationId", accommodationId);

            QuerySnapshot snapshot =
                await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<MaintenanceRequest>())
                .OrderByDescending(request => request.CreatedAtUtc)
                .ToList();
        }

        public async Task<MaintenanceRequest?> GetByIdAsync(
            string id,
            CancellationToken cancellationToken = default)
        {
            DocumentSnapshot document = await _firestore
                .Collection(CollectionName)
                .Document(id)
                .GetSnapshotAsync(cancellationToken);

            return document.Exists
                ? document.ConvertTo<MaintenanceRequest>()
                : null;
        }

        public async Task<MaintenanceRequest?> UpdateStatusAsync(
            string id,
            string status,
            CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(id);

            DocumentSnapshot document =
                await documentReference.GetSnapshotAsync(cancellationToken);

            if (!document.Exists)
            {
                return null;
            }

            DateTime updatedAtUtc = DateTime.UtcNow;

            await documentReference.UpdateAsync(
                new Dictionary<string, object>
                {
                    ["Status"] = status,
                    ["UpdatedAtUtc"] = updatedAtUtc
                },
                cancellationToken: cancellationToken);

            MaintenanceRequest request =
                document.ConvertTo<MaintenanceRequest>();

            request.Status = status;
            request.UpdatedAtUtc = updatedAtUtc;

            return request;
        }
    }
}