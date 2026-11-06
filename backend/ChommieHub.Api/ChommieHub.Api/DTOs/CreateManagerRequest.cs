using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Defines the information required to create a residence manager profile
    /// linked to a Firebase UID. Mirrors CreateStudentRequest's identity
    /// contract: the Firebase UID becomes Manager.Id.
    /// </summary>
    public sealed class CreateManagerRequest
    {
        [Required]
        public string FirebaseUid { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string Name { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string Surname { get; set; } = string.Empty;

        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Required]
        [StringLength(30)]
        public string PhoneNumber { get; set; } = string.Empty;

        // Optional: a newly appointed manager starts without an accommodation.
        public string AccommodationId { get; set; } = string.Empty;
    }
}
