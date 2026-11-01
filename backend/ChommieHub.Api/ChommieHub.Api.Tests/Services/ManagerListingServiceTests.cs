using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services;

// Verifies the manager's draft, edit and submit-for-review rules.
public sealed class ManagerListingServiceTests
{
    private readonly Mock<IManagerRepository> _managerRepository = new();
    private readonly Mock<IAccommodationRepository> _accommodationRepository = new();

    private ManagerListingService CreateService()
    {
        return new ManagerListingService(_managerRepository.Object, _accommodationRepository.Object);
    }

    private static CreateAccommodationRequest CreateRequest()
    {
        return new CreateAccommodationRequest
        {
            Name = "Yale Village",
            Address = "1 Main Road",
            Latitude = -25.7,
            Longitude = 28.2,
            Description = "Close to campus",
            PhoneNumber = "0123456789",
            InstitutionArea = "Hatfield"
        };
    }

    private void GivenManager(string accommodationId = "")
    {
        _managerRepository
            .Setup(repository => repository.GetByIdAsync("m1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new Manager { Id = "m1", FirebaseUid = "m1", AccommodationId = accommodationId });
    }

    private void GivenAccommodation(string status, string name = "Yale Village")
    {
        _accommodationRepository
            .Setup(repository => repository.GetByIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new Accommodation
            {
                Id = "a1",
                Name = name,
                Address = "1 Main Road",
                Description = "Close to campus",
                InstitutionArea = "Hatfield",
                Latitude = -25.7,
                Longitude = 28.2,
                ReviewStatus = status,
                ReviewReason = status == ListingStatuses.Rejected ? "Add more details" : string.Empty
            });
    }

    [Fact]
    public async Task SaveDraftAsync_WithoutAccommodation_CreatesDraftAndLinksManager()
    {
        GivenManager();
        _accommodationRepository
            .Setup(repository => repository.CreateAsync(It.IsAny<Accommodation>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync((Accommodation accommodation, CancellationToken _) =>
            {
                accommodation.Id = "a1";
                return accommodation;
            });

        ManagerAccommodationResponse result = await CreateService().SaveDraftAsync("m1", CreateRequest());

        Assert.Equal(ListingStatuses.Draft, result.ReviewStatus);
        _managerRepository.Verify(
            repository => repository.UpdateAsync(
                It.Is<Manager>(manager => manager.AccommodationId == "a1"),
                It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task SaveDraftAsync_WhenPending_IsRejected()
    {
        GivenManager("a1");
        GivenAccommodation(ListingStatuses.Pending);

        await Assert.ThrowsAsync<InvalidOperationException>(
            () => CreateService().SaveDraftAsync("m1", CreateRequest()));
    }

    [Fact]
    public async Task SaveDraftAsync_WhenRejected_UpdatesDetailsAndKeepsStatus()
    {
        GivenManager("a1");
        GivenAccommodation(ListingStatuses.Rejected, name: "Old name");

        ManagerAccommodationResponse result = await CreateService().SaveDraftAsync("m1", CreateRequest());

        Assert.Equal("Yale Village", result.Name);
        Assert.Equal(ListingStatuses.Rejected, result.ReviewStatus);
    }

    [Fact]
    public async Task SubmitForReviewAsync_WithIncompleteDetails_IsRejected()
    {
        GivenManager("a1");
        GivenAccommodation(ListingStatuses.Draft, name: "");

        await Assert.ThrowsAsync<ArgumentException>(
            () => CreateService().SubmitForReviewAsync("m1"));
    }

    [Fact]
    public async Task SubmitForReviewAsync_FromRejected_SetsPendingAndClearsReason()
    {
        GivenManager("a1");
        GivenAccommodation(ListingStatuses.Rejected);

        ManagerAccommodationResponse result = await CreateService().SubmitForReviewAsync("m1");

        Assert.Equal(ListingStatuses.Pending, result.ReviewStatus);
        Assert.Equal(string.Empty, result.ReviewReason);
        Assert.NotNull(result.SubmittedAtUtc);
    }

    [Fact]
    public async Task SubmitForReviewAsync_WhenAlreadyPending_IsRejected()
    {
        GivenManager("a1");
        GivenAccommodation(ListingStatuses.Pending);

        await Assert.ThrowsAsync<InvalidOperationException>(
            () => CreateService().SubmitForReviewAsync("m1"));
    }

    [Fact]
    public async Task GetMyAccommodationAsync_WithNoAccommodation_ReturnsNull()
    {
        GivenManager();

        ManagerAccommodationResponse? result = await CreateService().GetMyAccommodationAsync("m1");

        Assert.Null(result);
    }

    [Fact]
    public async Task GetMyAccommodationAsync_ForUnknownManager_Throws()
    {
        _managerRepository
            .Setup(repository => repository.GetByIdAsync("m1", It.IsAny<CancellationToken>()))
            .ReturnsAsync((Manager?)null);

        await Assert.ThrowsAsync<KeyNotFoundException>(
            () => CreateService().GetMyAccommodationAsync("m1"));
    }
}