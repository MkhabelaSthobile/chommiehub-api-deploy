using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Stores and retrieves digital room inspections from Google Cloud Firestore.
    /// </summary>
    public sealed class FirestoreInspectionRepository : IInspectionRepository
    {
        private const string CollectionName = "inspections";
        private readonly FirestoreDb _firestore;

        public FirestoreInspectionRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<Inspection> CreateAsync(Inspection inspection, CancellationToken cancellationToken = default)
        {
            DocumentReference document = _firestore.Collection(CollectionName).Document();
            inspection.Id = document.Id;

            await document.SetAsync(inspection, cancellationToken: cancellationToken);

            return inspection;
        }

        public async Task<IReadOnlyList<Inspection>> GetByResidentIdAsync(string residentId, CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore.Collection(CollectionName).WhereEqualTo("ResidentId", residentId).GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Inspection>())
                .OrderByDescending(inspection => inspection.CreatedAtUtc)
                .ToList();
        }

        public async Task<IReadOnlyList<Inspection>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore.Collection(CollectionName).WhereEqualTo("AccommodationId", accommodationId).GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Inspection>())
                .OrderByDescending(inspection => inspection.CreatedAtUtc)
                .ToList();
        }

        public async Task<Inspection?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            DocumentSnapshot snapshot = await _firestore.Collection(CollectionName).Document(id).GetSnapshotAsync(cancellationToken);

            return snapshot.Exists ? snapshot.ConvertTo<Inspection>() : null;
        }

        public async Task<Inspection?> ConfirmAsync(string id, string managerNotes, CancellationToken cancellationToken = default)
        {
            DocumentReference document = _firestore.Collection(CollectionName).Document(id);
            DocumentSnapshot snapshot = await document.GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return null;
            }

            Inspection inspection = snapshot.ConvertTo<Inspection>();
            inspection.ManagerConfirmed = true;
            inspection.ManagerNotes = managerNotes;
            inspection.Status = "Confirmed";
            inspection.ConfirmedAtUtc = DateTime.UtcNow;

            await document.SetAsync(inspection, cancellationToken: cancellationToken);

            return inspection;
        }
    }
}