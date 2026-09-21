using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore implementation used to store and retrieve visitor requests.
    /// </summary>
    public sealed class FirestoreVisitorRequestRepository : IVisitorRequestRepository
    {
        private const string CollectionName = "visitorRequests";

        private readonly FirestoreDb _firestore;

        public FirestoreVisitorRequestRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<VisitorRequest> CreateAsync(
            VisitorRequest request,
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

        public async Task<IReadOnlyList<VisitorRequest>> GetByResidentIdAsync(
            string residentId,
            CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("ResidentId", residentId);

            QuerySnapshot snapshot =
                await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<VisitorRequest>())
                .OrderByDescending(request => request.CreatedAtUtc)
                .ToList();
        }

        public async Task<IReadOnlyList<VisitorRequest>> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("AccommodationId", accommodationId);

            QuerySnapshot snapshot =
                await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<VisitorRequest>())
                .OrderByDescending(request => request.CreatedAtUtc)
                .ToList();
        }

        public async Task<VisitorRequest?> GetByIdAsync(
            string id,
            CancellationToken cancellationToken = default)
        {
            DocumentSnapshot document = await _firestore
                .Collection(CollectionName)
                .Document(id)
                .GetSnapshotAsync(cancellationToken);

            return document.Exists
                ? document.ConvertTo<VisitorRequest>()
                : null;
        }

        public async Task<VisitorRequest?> UpdateStatusAsync(
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

            DateTime reviewedAtUtc = DateTime.UtcNow;

            await documentReference.UpdateAsync(
                new Dictionary<string, object>
                {
                    ["Status"] = status,
                    ["ReviewedAtUtc"] = reviewedAtUtc
                },
                cancellationToken: cancellationToken);

            VisitorRequest request =
                document.ConvertTo<VisitorRequest>();

            request.Status = status;
            request.ReviewedAtUtc = reviewedAtUtc;

            return request;
        }
    }
}