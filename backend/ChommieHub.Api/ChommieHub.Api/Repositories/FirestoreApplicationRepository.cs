using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore implementation used to retrieve and update student accommodation applications
    /// </summary>
    public sealed class FirestoreApplicationRepository : IApplicationRepository
    {
        private const string CollectionName = "applications";

        private readonly FirestoreDb _firestore;

        public FirestoreApplicationRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<IReadOnlyList<Application>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("AccommodationId", accommodationId);

            QuerySnapshot snapshot = await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Application>())
                .OrderByDescending(application => application.DateSubmittedUtc)
                .ToList();
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