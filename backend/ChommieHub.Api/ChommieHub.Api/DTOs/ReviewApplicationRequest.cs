using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs
{
    public sealed class ReviewApplicationRequest
    {
        [Required]
        public string Decision { get; set; } = string.Empty;

        [StringLength(1000)]
        public string ReviewNotes { get; set; } = string.Empty;
    }
}