using System.ComponentModel.DataAnnotations;
using ChommieHub.Api.DTOs;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Guards the public validation contract for the new incident and announcement forms.
/// Authorization and Firebase/Firestore integration require additional runtime tests.
/// </summary>
public sealed class CommunityRequestValidationTests
{
    private static bool IsValid(object model)
    {
        var results = new List<ValidationResult>();
        return Validator.TryValidateObject(model, new ValidationContext(model), results, true);
    }

    [Fact]
    public void IncidentRejectsMissingDescriptionOrLocation()
    {
        Assert.False(IsValid(new CreateIncidentRequest { Category = "Safety", Location = "", Description = "Short" }));
    }

    [Fact]
    public void IncidentAcceptsCompleteReport()
    {
        Assert.True(IsValid(new CreateIncidentRequest
        {
            Category = "Safety", Location = "Main gate", Description = "Broken security gate after hours"
        }));
    }

    [Fact]
    public void AnnouncementRejectsEmptyTitle()
    {
        Assert.False(IsValid(new CreateAnnouncementRequest
        {
            Type = "Meeting", Title = "", Description = "There will be a residents meeting."
        }));
    }

    [Fact]
    public void AnnouncementAcceptsResidenceNotice()
    {
        Assert.True(IsValid(new CreateAnnouncementRequest
        {
            AccommodationId = "residence-1", Type = "Maintenance", Title = "Water interruption",
            Description = "Water supply may be interrupted from 9 until 10."
        }));
    }
}
