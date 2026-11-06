using System.ComponentModel.DataAnnotations;

namespace ChommieHub.Api.DTOs;

/// <summary>Fields entered by a resident when reporting an incident.</summary>
public sealed class CreateIncidentRequest
{
    [Required, MaxLength(32)] public string Category { get; set; } = string.Empty;
    [Required, StringLength(2000, MinimumLength = 8)] public string Description { get; set; } = string.Empty;
    [Required, MaxLength(200)] public string Location { get; set; } = string.Empty;
}

/// <summary>The incident decision and response recorded by the manager or administrator.</summary>
public sealed class ReviewIncidentRequest
{
    [Required, MaxLength(32)] public string Status { get; set; } = string.Empty;
    [MaxLength(1000)] public string ResolutionNote { get; set; } = string.Empty;
}

/// <summary>Notice fields; the server chooses the authorized author and audience.</summary>
public sealed class CreateAnnouncementRequest
{
    [MaxLength(120)] public string AccommodationId { get; set; } = string.Empty;
    [Required, MaxLength(40)] public string Type { get; set; } = string.Empty;
    [Required, StringLength(120, MinimumLength = 3)] public string Title { get; set; } = string.Empty;
    [Required, StringLength(2000, MinimumLength = 4)] public string Description { get; set; } = string.Empty;
}
