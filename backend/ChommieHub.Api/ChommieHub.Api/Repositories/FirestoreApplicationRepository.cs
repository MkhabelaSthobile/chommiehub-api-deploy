using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    public sealed class FirestoreApplicationRepository : IApplicationRepository
    {
        private const string CollectionName = "applications";

        private readonly FirestoreDb _firestore;

        public FirestoreApplicationRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<Application?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            DocumentSnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .Document(id)
                .GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return null;
            }

            return snapshot.ConvertTo<Application>();
        }

        public async Task<IReadOnlyList<Application>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .WhereEqualTo("AccommodationId", accommodationId)
                .GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Application>())
                .ToList();
        }

        public async Task<IReadOnlyList<Application>> GetByStudentIdAsync(string studentId, CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .WhereEqualTo("StudentId", studentId)
                .GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Application>())
                .ToList();
        }

        public async Task<Application> CreateAsync(Application application, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document();

            application.Id = documentReference.Id;

            await documentReference.SetAsync(
                application,
                cancellationToken: cancellationToken);

            return application;
        }

        public async Task<bool> UpdateAsync(Application application, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(application.Id);

            DocumentSnapshot snapshot = await documentReference.GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return false;
            }

            await documentReference.SetAsync(
                application,
                SetOptions.Overwrite,
                cancellationToken);

            return true;
        }
    }
}