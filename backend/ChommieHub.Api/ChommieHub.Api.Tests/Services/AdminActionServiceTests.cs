using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Moq;

namespace ChommieHub.Api.Tests.Services;

// Verifies the administrator's listing and account actions, including
// audit records and notifications, without Firestore or Firebase.

public sealed class AdminActionServiceTests
{
    private readonly Mock<IAccommodationRepository> _accommodations = new();
    private readonly Mock<IManagerRepository> _managers = new();
    private readonly Mock<IStudentRepository> _students = new();
    private readonly Mock<IApplicationRepository> _applications = new();
    private readonly Mock<IResidentRepository> _residents = new();
    private readonly Mock<IRoomRepository> _rooms = new();
    private readonly Mock<IAuditRepository> _audit = new();
    private readonly Mock<IFirebaseAccountAdmin> _accountAdmin = new();
    private readonly Mock<IUserNotifier> _notifier = new();

    private AdminActionService CreateService()
    {
        _audit.Setup(repository => repository.AddAsync(It.IsAny<AuditRecord>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync((AuditRecord record, CancellationToken _) => record);
        _residents.Setup(repository => repository.GetByAccommodationIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(Array.Empty<Resident>());
        _rooms.Setup(repository => repository.GetByAccommodationIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(Array.Empty<Room>());
        _applications.Setup(repository => repository.GetByAccommodationIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(Array.Empty<Application>());

        return new AdminActionService(
            _accommodations.Object,
            _managers.Object,
            _students.Object,
            _applications.Object,
            _residents.Object,
            _rooms.Object,
            _audit.Object,
            _accountAdmin.Object,
            _notifier.Object);
    }

    private Accommodation GivenListing(string status, bool archived = false)
    {
        var listing = new Accommodation
        {
            Id = "a1",
            Name = "Yale Village",
            ReviewStatus = status,
            IsArchived = archived
        };

        _accommodations.Setup(repository => repository.GetByIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(listing);

        return listing;
    }

    private void GivenListingManager()
    {
        _managers.Setup(repository => repository.GetByAccommodationIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new Manager { Id = "m1", FirebaseUid = "m1", AccommodationId = "a1" });
    }

    [Fact]
    public async Task ApproveListingAsync_WhenPending_ApprovesAuditsAndNotifies()
    {
        Accommodation listing = GivenListing(ListingStatuses.Pending);
        GivenListingManager();

        AdminActionResponse result = await CreateService().ApproveListingAsync("admin-1", "a1");

        Assert.Equal(ListingStatuses.Approved, listing.ReviewStatus);
        Assert.Equal("admin-1", listing.ReviewedByUid);
        Assert.Equal(AuditActions.ListingApproved, result.Action);
        _audit.Verify(
            repository => repository.AddAsync(
                It.Is<AuditRecord>(record =>
                    record.Action == AuditActions.ListingApproved
                    && record.AdminId == "admin-1"
                    && record.TargetId == "a1"),
                It.IsAny<CancellationToken>()),
            Times.Once);
        _notifier.Verify(
            notifier => notifier.NotifyAsync("m1", It.IsAny<string>(), It.IsAny<string>()),
            Times.Once);
    }

    [Fact]
    public async Task ApproveListingAsync_WhenNotPending_IsRejected()
    {
        GivenListing(ListingStatuses.Draft);

        await Assert.ThrowsAsync<InvalidOperationException>(
            () => CreateService().ApproveListingAsync("admin-1", "a1"));
    }

    [Fact]
    public async Task RejectListingAsync_RequiresAReason()
    {
        GivenListing(ListingStatuses.Pending);

        await Assert.ThrowsAsync<ArgumentException>(
            () => CreateService().RejectListingAsync("admin-1", "a1", "   "));
    }

    [Fact]
    public async Task RejectListingAsync_StoresTheReason()
    {
        Accommodation listing = GivenListing(ListingStatuses.Pending);
        GivenListingManager();

        await CreateService().RejectListingAsync("admin-1", "a1", " Add room photos ");

        Assert.Equal(ListingStatuses.Rejected, listing.ReviewStatus);
        Assert.Equal("Add room photos", listing.ReviewReason);
    }

    [Fact]
    public async Task ArchiveListingAsync_HidesTheListingAndRecordsTheReason()
    {
        Accommodation listing = GivenListing(ListingStatuses.Approved);
        GivenListingManager();

        await CreateService().ArchiveListingAsync("admin-1", "a1", "Closed for renovation");

        Assert.True(listing.IsArchived);
        _audit.Verify(
            repository => repository.AddAsync(
                It.Is<AuditRecord>(record =>
                    record.Action == AuditActions.ListingArchived
                    && record.Reason == "Closed for renovation"),
                It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task UnarchiveListingAsync_WhenNotArchived_IsRejected()
    {
        GivenListing(ListingStatuses.Approved);

        await Assert.ThrowsAsync<InvalidOperationException>(
            () => CreateService().UnarchiveListingAsync("admin-1", "a1"));
    }

    [Fact]
    public async Task DeleteListingAsync_WithRooms_IsRejected()
    {
        GivenListing(ListingStatuses.Approved);
        AdminActionService service = CreateService();
        _rooms.Setup(repository => repository.GetByAccommodationIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new[] { new Room { Id = "r1", AccommodationId = "a1" } });

        await Assert.ThrowsAsync<InvalidOperationException>(
            () => service.DeleteListingAsync("admin-1", "a1", "Duplicate listing"));

        _accommodations.Verify(
            repository => repository.DeleteAsync("a1", It.IsAny<CancellationToken>()),
            Times.Never);
    }

    [Fact]
    public async Task DeleteListingAsync_WhenEmpty_DeletesAndUnlinksTheManager()
    {
        GivenListing(ListingStatuses.Rejected);
        var manager = new Manager { Id = "m1", FirebaseUid = "m1", AccommodationId = "a1" };
        _managers.Setup(repository => repository.GetByAccommodationIdAsync("a1", It.IsAny<CancellationToken>()))
            .ReturnsAsync(manager);

        await CreateService().DeleteListingAsync("admin-1", "a1", "Duplicate listing");

        _accommodations.Verify(
            repository => repository.DeleteAsync("a1", It.IsAny<CancellationToken>()),
            Times.Once);
        Assert.Equal(string.Empty, manager.AccommodationId);
    }
}