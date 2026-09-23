using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Request submitted by a resident when completing a move-in or move-out inspection.
    /// </summary>
    public sealed class CreateInspectionRequest
    {
        [Required]
        public string ResidentId { get; set; } = string.Empty;

        [Required]
        public string StudentId { get; set; } = string.Empty;

        [Required]
        public string AccommodationId { get; set; } = string.Empty;

        [Required]
        public string RoomId { get; set; } = string.Empty;

        [Required]
        public string Type { get; set; } = string.Empty;

        [MinLength(1)]
        public List<InspectionItemRequest> Items { get; set; } = [];
    }

    /// <summary>
    /// Condition information captured for one room item.
    /// </summary>
    public sealed class InspectionItemRequest
    {
        [Required]
        public string ItemName { get; set; } = string.Empty;

        [Required]
        public string Condition { get; set; } = string.Empty;

        public string Comments { get; set; } = string.Empty;

        public string PhotoReference { get; set; } = string.Empty;
    }

    /// <summary>
    /// Request used by a residence manager to confirm an inspection.
    /// </summary>
    public sealed class ConfirmInspectionRequest
    {
        public string ManagerNotes { get; set; } = string.Empty;
    }

    /// <summary>
    /// Room-item information returned to Android.
    /// </summary>
    public sealed class InspectionItemResponse
    {
        public string ItemName { get; init; } = string.Empty;
        public string Condition { get; init; } = string.Empty;
        public string Comments { get; init; } = string.Empty;
        public string PhotoReference { get; init; } = string.Empty;
    }

    /// <summary>
    /// Complete inspection returned by the REST API.
    /// </summary>
    public sealed class InspectionResponse
    {
        public string Id { get; init; } = string.Empty;
        public string ResidentId { get; init; } = string.Empty;
        public string StudentId { get; init; } = string.Empty;
        public string AccommodationId { get; init; } = string.Empty;
        public string RoomId { get; init; } = string.Empty;
        public string Type { get; init; } = string.Empty;
        public List<InspectionItemResponse> Items { get; init; } = [];
        public string Status { get; init; } = string.Empty;
        public bool ManagerConfirmed { get; init; }
        public string ManagerNotes { get; init; } = string.Empty;
        public DateTime CreatedAtUtc { get; init; }
        public DateTime? ConfirmedAtUtc { get; init; }
    }
}