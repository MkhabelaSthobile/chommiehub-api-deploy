using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Defines the information submitted by a student when applying for a specific room.
    /// The server resolves the room type from the selected room so Android cannot submit a room type that does not match the selected Firestore room.
    /// </summary>
    public sealed class SubmitApplicationRequest
    {
        [Required]
        public string AccommodationId { get; set; } = string.Empty;

        [Required]
        public string RoomId { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string RentalPeriod { get; set; } = string.Empty;

        [StringLength(2000)]
        public string SupportingInfo { get; set; } = string.Empty;
    }
}