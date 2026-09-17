using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    ///Defines validated accommodation values that may be updated by management
    /// </summary>
    public sealed class UpdateAccommodationRequest
    {
        [Required]
        [StringLength(120)]
        public string Name { get; set; } = string.Empty;

        [Required]
        [StringLength(250)]
        public string Address { get; set; } = string.Empty;

        [Range(-90, 90)]
        public double Latitude { get; set; }

        [Range(-180, 180)]
        public double Longitude { get; set; }

        [Required]
        [StringLength(1500)]
        public string Description { get; set; } = string.Empty;

        [StringLength(30)]
        public string PhoneNumber { get; set; } = string.Empty;

        public List<string> Amenities { get; set; } = [];

        [Required]
        [StringLength(120)]
        public string InstitutionArea { get; set; } = string.Empty;

        public bool IsActive { get; set; }
    }
}