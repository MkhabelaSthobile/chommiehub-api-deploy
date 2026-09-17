using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    /// <summary>
    /// Firestore implementation of accommodation persistence.
    /// Database-specific operations remain isolated from controllers and services.
    /// </summary>
    public sealed class FirestoreAccommodationRepository : IAccommodationRepository
    {
        private const string CollectionName = "accommodations";

        private readonly FirestoreDb _firestore;

        public FirestoreAccommodationRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<IReadOnlyList<Accommodation>> GetAllAsync(CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Accommodation>())
                .ToList();
        }

        public async Task<Accommodation?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(id);

            DocumentSnapshot snapshot = await documentReference.GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return null;
            }

            return snapshot.ConvertTo<Accommodation>();
        }

        public async Task<Accommodation> CreateAsync(Accommodation accommodation, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document();

            accommodation.Id = documentReference.Id;

            await documentReference.SetAsync(accommodation, cancellationToken: cancellationToken);

            return accommodation;
        }

        public async Task<bool> UpdateAsync(Accommodation accommodation, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(accommodation.Id);

            DocumentSnapshot snapshot = await documentReference.GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return false;
            }

            await documentReference.SetAsync(
                accommodation,
                SetOptions.Overwrite,
                cancellationToken);

            return true;
        }
    }
}