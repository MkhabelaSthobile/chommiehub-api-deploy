using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Repositories.Interfaces;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies that a student's shortlist hides listings they can no longer see.
/// </summary>
public sealed class ShortlistServiceTests
{
    private readonly Mock<IShortlistRepository> _shortlistRepository = new();
    private readonly Mock<IAccommodationRepository> _accommodationRepository = new();

    [Fact]
    public async Task GetShortlistAsync_SkipsArchivedUnapprovedAndMissingListings()
    {
        _shortlistRepository
            .Setup(repository => repository.GetAccommodationIdsAsync("s1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new[] { "approved", "archived", "pending", "missing" });

        GivenListing("approved", ListingStatuses.Approved, archived: false);
        GivenListing("archived", ListingStatuses.Approved, archived: true);
        GivenListing("pending", ListingStatuses.Pending, archived: false);
        _accommodationRepository
            .Setup(repository => repository.GetByIdAsync("missing", It.IsAny<CancellationToken>()))
            .ReturnsAsync((Accommodation?)null);

        var service = new ShortlistService(_shortlistRepository.Object, _accommodationRepository.Object);

        var result = await service.GetShortlistAsync("s1");

        Assert.Equal("approved", Assert.Single(result).Id);
    }

    [Fact]
    public async Task AddAsync_RejectsPendingAccommodation()
    {
        GivenListing("pending", ListingStatuses.Pending, archived: false);
        var service = new ShortlistService(_shortlistRepository.Object, _accommodationRepository.Object);

        await Assert.ThrowsAsync<InvalidOperationException>(
            () => service.AddAsync("s1", "pending"));

        _shortlistRepository.Verify(repository => repository.AddAsync(
            It.IsAny<string>(), It.IsAny<string>(), It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task AddAsync_AllowsApprovedAccommodation()
    {
        GivenListing("approved", ListingStatuses.Approved, archived: false);
        var service = new ShortlistService(_shortlistRepository.Object, _accommodationRepository.Object);

        await service.AddAsync("s1", "approved");

        _shortlistRepository.Verify(repository => repository.AddAsync(
            "s1", "approved", It.IsAny<CancellationToken>()), Times.Once);
    }

    private void GivenListing(string id, string status, bool archived)
    {
        _accommodationRepository
            .Setup(repository => repository.GetByIdAsync(id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new Accommodation
            {
                Id = id,
                Name = id,
                ReviewStatus = status,
                IsArchived = archived
            });
    }
}