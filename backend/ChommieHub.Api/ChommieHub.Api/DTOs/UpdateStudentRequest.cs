using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Defines the fields a student may edit on their own profile. Identity fields
    /// (Firebase UID, accommodation, room, user type) are not editable through this request.
    /// </summary>
    public sealed class UpdateStudentRequest
    {
        [Required]
        [StringLength(100)]
        public string Name { get; set; } = string.Empty;

        [Required]
        [StringLength(100)]
        public string Surname { get; set; } = string.Empty;

        [Required]
        [StringLength(30)]
        public string PhoneNumber { get; set; } = string.Empty;

        [StringLength(50)]
        public string PreferredLanguage { get; set; } = "English";
    }
}
