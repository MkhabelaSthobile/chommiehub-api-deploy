namespace ChommieHub.Api.DTOs
{
    public sealed record AuditRecordResponse(
        string Id,
        string AdminId,
        string Action,
        string TargetType,
        string TargetId,
        string TargetName,
        string Reason,
        DateTime CreatedAtUtc);

    public sealed record AdminUserSummary(
        string Id,
        string Name,
        string Surname,
        string Email,
        string PhoneNumber,
        string Role,
        string AccommodationId,
        string AccommodationName,
        bool IsDisabled,
        DateTime CreatedAtUtc);

    public sealed record AdminListingSummary(
        string Id,
        string Name,
        string Address,
        string InstitutionArea,
        string ReviewStatus,
        bool IsArchived,
        bool IsActive,
        string ManagerId,
        string ManagerName,
        DateTime? SubmittedAtUtc,
        DateTime? ReviewedAtUtc);

    public sealed record AdminApplicationSummary(
        string Id,
        string AccommodationId,
        string AccommodationName,
        string Status,
        DateTime DateSubmittedUtc);

    public sealed record AdminRoomSummary(
        string Id,
        string RoomType,
        double Price,
        int Capacity,
        bool Availability);

    public sealed record AdminUserDetailResponse(
        AdminUserSummary User,
        AdminListingSummary? Accommodation,
        IReadOnlyList<AdminApplicationSummary> Applications);

    public sealed record AdminListingDetailResponse(
        ManagerAccommodationResponse Accommodation,
        AdminUserSummary? Manager,
        IReadOnlyList<AdminUserSummary> Residents,
        IReadOnlyList<AdminRoomSummary> Rooms,
        int ApplicationCount);

    public sealed record AdminDashboardResponse(
        int Students,
        int Residents,
        int Managers,
        int ListingsTotal,
        int ListingsDraft,
        int ListingsPending,
        int ListingsApproved,
        int ListingsRejected,
        int ListingsArchived,
        int ApplicationsTotal,
        int ApplicationsOpen,
        IReadOnlyList<AuditRecordResponse> RecentActivity);
}