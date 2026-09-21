using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories.Interfaces;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    //Firestore implementation of shortlist persistence.
    public sealed class FirestoreShortlistRepository : IShortlistRepository
    {
        private const string CollectionName = "shortlists";

        private readonly FirestoreDb _firestore;

        public FirestoreShortlistRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task AddAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(BuildDocumentId(studentId, accommodationId));

            var shortlist = new Shortlist
            {
                StudentId = studentId,
                AccommodationId = accommodationId,
                DateAddedUtc = DateTime.UtcNow
            };

            await documentReference.SetAsync(shortlist, cancellationToken: cancellationToken);
        }

        public async Task RemoveAsync(string studentId, string accommodationId, CancellationToken cancellationToken = default)
        {
            DocumentReference documentReference = _firestore
                .Collection(CollectionName)
                .Document(BuildDocumentId(studentId, accommodationId));

            await documentReference.DeleteAsync(cancellationToken: cancellationToken);
        }

        public async Task<IReadOnlyList<string>> GetAccommodationIdsAsync(string studentId, CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .WhereEqualTo("StudentId", studentId)
                .GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<Shortlist>().AccommodationId)
                .ToList();
        }

        private static string BuildDocumentId(string studentId, string accommodationId)
        {
            return $"{studentId}_{accommodationId}";
        }
    }
}
