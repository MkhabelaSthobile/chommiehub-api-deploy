namespace ChommieHub.Api.DTOs
{
    /// <summary>
    ///Provides summary information used by the manager application list.
    /// </summary>
    public sealed class ManagerApplicationSummaryResponse
    {
        public string Id { get; init; } = string.Empty;

        public string StudentId { get; init; } = string.Empty;

        public string AccommodationId { get; init; } = string.Empty;

        public string RoomTypePreference { get; init; } = string.Empty;

        public string Status { get; init; } = string.Empty;

        public string AssignedRoomId { get; init; } = string.Empty;

        public DateTime DateSubmittedUtc { get; init; }
    }
}