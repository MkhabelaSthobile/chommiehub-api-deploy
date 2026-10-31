using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    // Details a new residence manager enters when signing up. The Firebase UID
    // comes from the verified ID token, not from this request.     
    public sealed class RegisterManagerRequest
    {
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
    }
}