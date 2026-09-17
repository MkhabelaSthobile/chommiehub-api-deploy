using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    public sealed class CreateApplicationRequest
    {
        [Required]
        public string StudentId { get; set; } = string.Empty;

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