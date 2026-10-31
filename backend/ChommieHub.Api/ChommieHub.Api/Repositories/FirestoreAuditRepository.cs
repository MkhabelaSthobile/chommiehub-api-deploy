using ChommieHub.Api.Models;
using Google.Cloud.Firestore;

namespace ChommieHub.Api.Repositories
{
    public sealed class FirestoreAuditRepository : IAuditRepository
    {
        private const string CollectionName = "auditLogs";

        private readonly FirestoreDb _firestore;

        public FirestoreAuditRepository(FirestoreDb firestore)
        {
            _firestore = firestore;
        }

        public async Task<AuditRecord> AddAsync(
            AuditRecord record,
            CancellationToken cancellationToken = default)
        {
            record.CreatedAtUtc = DateTime.UtcNow;

            DocumentReference reference = await _firestore
                .Collection(CollectionName)
                .AddAsync(record, cancellationToken);

            record.Id = reference.Id;
            return record;
        }

        public async Task<IReadOnlyList<AuditRecord>> GetRecentAsync(
            int limit,
            CancellationToken cancellationToken = default)
        {
            QuerySnapshot snapshot = await _firestore
                .Collection(CollectionName)
                .OrderByDescending("CreatedAtUtc")
                .Limit(limit)
                .GetSnapshotAsync(cancellationToken);

            return snapshot.Documents
                .Select(document => document.ConvertTo<AuditRecord>())
                .ToList();
        }
    }
}