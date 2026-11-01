using ChommieHub.Api.DTOs;
using ChommieHub.Api.Models;
using ChommieHub.Api.Repositories;
using ChommieHub.Api.Services;
using Moq;

namespace ChommieHub.Api.Tests.Services;

// Verifies the administrator's read-only views without a live Firestore connection.

public sealed class AdminQueryServiceTests
{
    private readonly Mock<IStudentRepository> _students = new();
    private readonly Mock<IManagerRepository> _managers = new();
    private readonly Mock<IAccommodationRepository> _accommodations = new();
    private readonly Mock<IApplicationRepository> _applications = new();
    private readonly Mock<IResidentRepository> _residents = new();
    private readonly Mock<IRoomRepository> _rooms = new();
    private readonly Mock<IAuditRepository> _audit = new();

    private AdminQueryService CreateService(
        IReadOnlyList<Student>? students = null,
        IReadOnlyList<Manager>? managers = null,
        IReadOnlyList<Accommodation>? accommodations = null,
        IReadOnlyList<Application>? applications = null)
    {
        _students.Setup(repository => repository.GetAllAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(students ?? Array.Empty<Student>());
        _managers.Setup(repository => repository.GetAllAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(managers ?? Array.Empty<Manager>());
        _accommodations.Setup(repository => repository.GetAllAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(accommodations ?? Array.Empty<Accommodation>());
        _applications.Setup(repository => repository.GetAllAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(applications ?? Array.Empty<Application>());
        _audit.Setup(repository => repository.GetRecentAsync(It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(Array.Empty<AuditRecord>());

        return new AdminQueryService(
            _students.Object,
            _managers.Object,
            _accommodations.Object,
            _applications.Object,
            _residents.Object,
            _rooms.Object,
            _audit.Object);
    }

    private static Student MakeStudent(string id, string name, string userType = "Applicant") =>
        new() { Id = id, Name = name, Surname = "Smith", Email = $"{name.ToLower()}@example.com", UserType = userType };

    private static Accommodation MakeListing(string id, string status, bool archived = false) =>
        new() { Id = id, Name = $"Residence {id}", ReviewStatus = status, IsArchived = archived };

    [Fact]
    public async Task GetDashboardAsync_CountsAccountsListingsAndApplications()
    {
        AdminQueryService service = CreateService(
            students:
            [
                MakeStudent("s1", "Anna"),
                MakeStudent("s2", "Ben"),
                MakeStudent("s3", "Cara", "Resident")
            ],
            managers: [new Manager { Id = "m1", Name = "Dan" }],
            accommodations:
            [
                MakeListing("a1", ListingStatuses.Approved),
                MakeListing("a2", ListingStatuses.Pending),
                MakeListing("a3", ListingStatuses.Rejected),
                MakeListing("a4", ListingStatuses.Approved, archived: true),
                MakeListing("a5", ListingStatuses.Draft)
            ],
            applications:
            [
                new Application { Id = "p1", Status = ApplicationStatuses.Submitted },
                new Application { Id = "p2", Status = ApplicationStatuses.UnderReview },
                new Application { Id = "p3", Status = ApplicationStatuses.Approved }
            ]);

        AdminDashboardResponse result = await service.GetDashboardAsync();

        Assert.Equal(2, result.Students);
        Assert.Equal(1, result.Residents);
        Assert.Equal(1, result.Managers);
        Assert.Equal(5, result.ListingsTotal);
        Assert.Equal(1, result.ListingsDraft);
        Assert.Equal(1, result.ListingsPending);
        Assert.Equal(1, result.ListingsApproved);
        Assert.Equal(1, result.ListingsRejected);
        Assert.Equal(1, result.ListingsArchived);
        Assert.Equal(3, result.ApplicationsTotal);
        Assert.Equal(2, result.ApplicationsOpen);
    }

    [Fact]
    public async Task GetUsersAsync_FiltersByRole()
    {
        AdminQueryService service = CreateService(
            students: [MakeStudent("s1", "Anna"), MakeStudent("s2", "Cara", "Resident")],
            managers: [new Manager { Id = "m1", Name = "Dan" }]);

        IReadOnlyList<AdminUserSummary> result = await service.GetUsersAsync("resident", null);

        AdminUserSummary user = Assert.Single(result);
        Assert.Equal("s2", user.Id);
        Assert.Equal(AccountRoles.Resident, user.Role);
    }

    [Fact]
    public async Task GetUsersAsync_WithInvalidRole_IsRejected()
    {
        AdminQueryService service = CreateService();

        await Assert.ThrowsAsync<ArgumentException>(() => service.GetUsersAsync("admin", null));
    }

    [Fact]
    public async Task GetUsersAsync_SearchMatchesNameOrEmail()
    {
        AdminQueryService service = CreateService(
            students: [MakeStudent("s1", "Anna"), MakeStudent("s2", "Ben")]);

        IReadOnlyList<AdminUserSummary> result = await service.GetUsersAsync(null, "ben@");

        Assert.Equal("s2", Assert.Single(result).Id);
    }

    [Fact]
    public async Task GetListingsAsync_ArchivedFilter_ReturnsOnlyArchived()
    {
        AdminQueryService service = CreateService(
            accommodations:
            [
                MakeListing("a1", ListingStatuses.Approved),
                MakeListing("a2", ListingStatuses.Approved, archived: true)
            ]);

        IReadOnlyList<AdminListingSummary> result = await service.GetListingsAsync("archived");

        Assert.Equal("a2", Assert.Single(result).Id);
    }

    [Fact]
    public async Task GetListingsAsync_PendingFilter_ExcludesArchivedListings()
    {
        AdminQueryService service = CreateService(
            accommodations:
            [
                MakeListing("a1", ListingStatuses.Pending),
                MakeListing("a2", ListingStatuses.Pending, archived: true)
            ]);

        IReadOnlyList<AdminListingSummary> result = await service.GetListingsAsync("Pending");

        Assert.Equal("a1", Assert.Single(result).Id);
    }

    [Fact]
    public async Task GetListingAsync_ForUnknownId_ReturnsNull()
    {
        CreateService();
        _accommodations.Setup(repository => repository.GetByIdAsync("missing", It.IsAny<CancellationToken>()))
            .ReturnsAsync((Accommodation?)null);

        AdminListingDetailResponse? result = await new AdminQueryService(
            _students.Object,
            _managers.Object,
            _accommodations.Object,
            _applications.Object,
            _residents.Object,
            _rooms.Object,
            _audit.Object).GetListingAsync("missing");

        Assert.Null(result);
    }

    [Fact]
    public async Task GetUserAsync_ForUnknownId_ReturnsNull()
    {
        AdminQueryService service = CreateService();
        _students.Setup(repository => repository.GetByIdAsync("missing", It.IsAny<CancellationToken>()))
            .ReturnsAsync((Student?)null);
        _managers.Setup(repository => repository.GetByIdAsync("missing", It.IsAny<CancellationToken>()))
            .ReturnsAsync((Manager?)null);

        AdminUserDetailResponse? result = await service.GetUserAsync("missing");

        Assert.Null(result);
    }

    [Fact]
    public async Task GetAuditAsync_CapsTheLimit()
    {
        AdminQueryService service = CreateService();

        await service.GetAuditAsync(1000);

        _audit.Verify(
            repository => repository.GetRecentAsync(200, It.IsAny<CancellationToken>()),
            Times.Once);
    }
}