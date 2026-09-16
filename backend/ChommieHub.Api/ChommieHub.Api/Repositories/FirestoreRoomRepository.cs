using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    public sealed class FirestoreRoomRepository : IRoomRepository
    {
        private const string CollectionName = "rooms";

        private readonly FirestoreDb _firestore;

        public FirestoreRoomRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<IReadOnlyList<Room>> GetByAccommodationIdAsync(string accommodationId, CancellationToken cancellationToken = default)
        {
            Query query = _firestore
                .Collection(CollectionName)
                .WhereEqualTo("AccommodationId", accommodationId);

            QuerySnapshot snapshot = await query.GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Room>())
                .ToList();
        }

        public async Task<Room?> GetByIdAsync(string id, CancellationToken cancellationToken = default)
        {
            DocumentSnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .Document(id)
                .GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return null;
            }

            return snapshot.ConvertTo<Room>();
        }

        public async Task<Room> CreateAsync(Room room, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document();

            room.Id = documentReference.Id;

            await documentReference.SetAsync(room, cancellationToken: cancellationToken);

            return room;
        }

        public async Task<bool> UpdateAsync(Room room, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(room.Id);

            DocumentSnapshot snapshot = await documentReference.GetSnapshotAsync(cancellationToken);

            if (!snapshot.Exists)
            {
                return false;
            }

            await documentReference.SetAsync(room, SetOptions.Overwrite, cancellationToken);

            return true;
        }
    }
}