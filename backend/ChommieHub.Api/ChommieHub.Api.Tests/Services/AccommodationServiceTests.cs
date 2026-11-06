using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies which accommodation listings students are allowed to see.
/// </summary>
public sealed class AccommodationServiceTests
{
    private readonly Mock<IAccommodationRepository> _repository = new();

    [Fact]
    public async Task GetAllAsync_ReturnsOnlyApprovedUnarchivedListings()
    {
        _repository
            .Setup(repository => repository.GetAllAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(new[]
            {
                new Accommodation { Id = "approved", Name = "A", ReviewStatus = ListingStatuses.Approved },
                new Accommodation { Id = "pending", Name = "B", ReviewStatus = ListingStatuses.Pending },
                new Accommodation { Id = "rejected", Name = "C", ReviewStatus = ListingStatuses.Rejected },
                new Accommodation { Id = "draft", Name = "D", ReviewStatus = ListingStatuses.Draft },
                new Accommodation { Id = "archived", Name = "E", ReviewStatus = ListingStatuses.Approved, IsArchived = true }
            });

        var result = await new AccommodationService(_repository.Object).GetAllAsync();

        Assert.Equal("approved", Assert.Single(result).Id);
    }

    [Fact]
    public async Task GetAllAsync_KeepsListingsSavedBeforeTheReviewWorkflow()
    {
        _repository
            .Setup(repository => repository.GetAllAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(new[] { new Accommodation { Id = "legacy", Name = "Legacy" } });

        var result = await new AccommodationService(_repository.Object).GetAllAsync();

        Assert.Equal("legacy", Assert.Single(result).Id);
    }

    [Fact]
    public async Task GetByIdAsync_HidesPendingListingFromPublicDetails()
    {
        _repository
            .Setup(repository => repository.GetByIdAsync("pending", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new Accommodation { Id = "pending", Name = "B", ReviewStatus = ListingStatuses.Pending });

        var result = await new AccommodationService(_repository.Object).GetByIdAsync("pending");

        Assert.Null(result);
    }
}