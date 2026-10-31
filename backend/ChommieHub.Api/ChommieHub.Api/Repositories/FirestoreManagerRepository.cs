using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore implementation used to persist ChommieHub residence manager profiles.
    /// </summary>
    public sealed class FirestoreManagerRepository : IManagerRepository
    {
        private const string CollectionName = "managers";

        private readonly FirestoreDb _firestore;

        public FirestoreManagerRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<Manager?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            DocumentSnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .Document(id)
                .GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return null;
            }

            return snapshot.ConvertTo<Manager>();
        }


        public async Task<Manager> CreateAsync(
            Manager manager,
            CancellationToken cancellationToken = default)
        {
            if (string.IsNullOrWhiteSpace(manager.FirebaseUid))
            {
                throw new ArgumentException(
                    "A Firebase UID is required to create a manager profile.",
                    nameof(manager));
            }

            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(manager.FirebaseUid);

            manager.Id = manager.FirebaseUid;
            manager.CreatedAtUtc = DateTime.UtcNow;
            manager.UpdatedAtUtc = DateTime.UtcNow;

            await documentReference.SetAsync(
                manager,
                cancellationToken: cancellationToken);

            return manager;
        }

        public async Task<IReadOnlyList<Manager>> GetAllAsync(CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Manager>())
                .ToList();
        }

        public async Task<Manager?> GetByAccommodationIdAsync(
            string accommodationId,
            CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .WhereEqualTo("AccommodationId", accommodationId)
                .Limit(1)
                .GetSnapshotAsync(cancellationToken);

            DocumentSnapshot? document = snapshot.Documents.FirstOrDefault();
            return document?.ConvertTo<Manager>();
        }

        public async Task<bool> UpdateAsync(Manager manager, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(manager.Id);

            DocumentSnapshot snapshot = await documentReference.GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return false;
            }

            await documentReference.SetAsync(
                manager,
                SetOptions.Overwrite,
                cancellationToken);

            return true;
        }
    }
}
