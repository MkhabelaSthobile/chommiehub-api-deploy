using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services.Interfaces;

namespace ChommieHub.Api.Services;

/// <summary>
/// Resolves trusted Firebase identity and residence scope for community API operations.
/// No client-submitted UID or email suffix is treated as proof of a role.
/// </summary>
public sealed class CommunityAccess
{
    private readonly FirebaseTokenVerificationService _tokens;
    private readonly IAdminAccessVerifier _admins;
    private readonly IManagerAccountService _managers;
    private readonly IResidentRepository _residents;

    public CommunityAccess(FirebaseTokenVerificationService tokens,
        IAdminAccessVerifier admins, IManagerAccountService managers,
        IResidentRepository residents)
    {
        _tokens = tokens;
        _admins = admins;
        _managers = managers;
        _residents = residents;
    }

    public async Task<CommunityIdentity?> ResolveAsync(string? authHeader, CancellationToken ct)
    {
        if (string.IsNullOrWhiteSpace(authHeader) ||
            !authHeader.StartsWith("Bearer ", StringComparison.OrdinalIgnoreCase)) return null;
        string token = authHeader[7..].Trim();
        if (token.Length == 0) return null;
        string? uid = await _tokens.VerifyUserIdAsync(token);
        if (string.IsNullOrWhiteSpace(uid)) return null;
        AdminVerification adminResult = await _admins.VerifyAsync(token);
        bool isAdmin = adminResult.Access == AdminAccess.Granted;
        var manager = isAdmin ? null : await _managers.GetByIdAsync(uid, ct);
        bool isManager = manager is not null && !manager.IsDisabled &&
            string.Equals(manager.FirebaseUid, uid, StringComparison.Ordinal) &&
            !string.IsNullOrWhiteSpace(manager.AccommodationId);
        Resident? resident = isAdmin || isManager ? null : await _residents.GetByStudentIdAsync(uid, ct);
        if (resident is not null && !resident.IsActive) resident = null;
        return new CommunityIdentity(uid, isAdmin, isManager ? manager!.AccommodationId : null, resident);
    }
}

public sealed record CommunityIdentity(string Uid, bool IsAdmin, string? ManagerAccommodationId,
    Resident? Resident)
{
    public bool IsManagerFor(string accommodationId) =>
        !string.IsNullOrWhiteSpace(ManagerAccommodationId) &&
        string.Equals(ManagerAccommodationId, accommodationId, StringComparison.Ordinal);
}
