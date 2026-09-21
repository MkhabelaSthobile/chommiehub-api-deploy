using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    /// <summary>
    /// Defines the student-facing settings introduced in fragment_student_settings: preferred
    /// language and whether notifications are enabled. Kept intentionally small so it does not
    /// invent functionality beyond what the Part 1 settings screen already specifies.
    /// </summary>
    public sealed class UpdateStudentSettingsRequest
    {
        [Required]
        [StringLength(50)]
        public string PreferredLanguage { get; set; } = "English";

        public bool NotificationsEnabled { get; set; } = true;
    }
}
