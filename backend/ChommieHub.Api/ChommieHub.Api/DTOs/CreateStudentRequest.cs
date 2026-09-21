using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Defines the information required to create a student profile linked to a Firebase UID.
    /// The Firebase UID is preserved as the Student.Id / Application.StudentId / Resident.StudentId
    /// identity contract described in the Person B implementation instructions.
    /// </summary>
    public sealed class CreateStudentRequest
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

        [StringLength(50)]
        public string PreferredLanguage { get; set; } = "English";
    }
}
