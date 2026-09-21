using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Defines the information a student submits when applying for accommodation. The student
    /// identifier is taken from the route (the signed-in Firebase UID) rather than the body, so a
    /// student cannot submit an application on behalf of another Student.Id.
    /// </summary>
    public sealed class SubmitApplicationRequest
    {
        [Required]
        public string AccommodationId { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string RoomTypePreference { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string RentalPeriod { get; set; } = string.Empty;

        [StringLength(2000)]
        public string SupportingInfo { get; set; } = string.Empty;
    }
}
