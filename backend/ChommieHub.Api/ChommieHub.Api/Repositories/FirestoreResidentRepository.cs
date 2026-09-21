using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore implementation used to store and retrieve resident records.
    /// </summary>
    public sealed class FirestoreResidentRepository : IResidentRepository
    {
        private const string CollectionName = "residents";

        private readonly FirestoreDb _firestore;

        public FirestoreResidentRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<IReadOnlyList<Resident>> GetByAccommodationIdAsync(string accommodationId,  CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("AccommodationId", accommodationId);

            QuerySnapshot snapshot = await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Resident>())
                .ToList();
        }

        public async Task<Resident?> GetByApplicationIdAsync(string applicationId,
            CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .WhereEqualTo("ApplicationId", applicationId)
                .GetSnapshotAsync(cancellationToken);

            DocumentSnapshot? document = snapshot.Documents.FirstOrDefault();

            return document is null
                ? null : document.ConvertTo<Resident>();
        }

        public async Task<Resident?> GetByStudentIdAsync(
    string studentId,
    CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .WhereEqualTo("StudentId", studentId)
                .WhereEqualTo("IsActive", true)
                .GetSnapshotAsync(cancellationToken);

            DocumentSnapshot? document =
                snapshot.Documents.FirstOrDefault();

            return document is null
                ? null
                : document.ConvertTo<Resident>();
        }

        public async Task<Resident> CreateAsync(Resident resident, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference =  _firestore
                .Collection(CollectionName)
                .Document();

            resident.Id = documentReference.Id;

            await documentReference.SetAsync(
                resident,
                cancellationToken : cancellationToken);

            return resident;
        }
    }
}