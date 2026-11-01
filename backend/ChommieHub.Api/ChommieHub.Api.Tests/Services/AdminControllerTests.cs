using ChommieHub.Api.Controllers;
using ChommieHub.Api.DTOs;
using ChommieHub.Api.Services;
using ChommieHub.Api.Services.Interfaces;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Moq;

namespace ChommieHub.Api.Tests.Controllers;

/// <summary>
/// Verifies the administrator endpoints: access checks and status codes.
/// </summary>
public sealed class AdminControllerTests
{
    private readonly Mock<IAdminAccessVerifier> _verifier = new();
    private readonly Mock<IAdminQueryService> _query = new();
    private readonly Mock<IAdminActionService> _actions = new();

    private AdminController CreateController(
        AdminVerification verification,
        string? authorization = "Bearer token")
    {
        _verifier
            .Setup(verifier => verifier.VerifyAsync(It.IsAny<string?>()))
            .ReturnsAsync(verification);

        var controller = new AdminController(_verifier.Object, _query.Object, _actions.Object)
        {
            ControllerContext = new ControllerContext { HttpContext = new DefaultHttpContext() }
        };

        if (authorization is not null)
        {
            controller.ControllerContext.HttpContext.Request.Headers.Authorization = authorization;
        }

        return controller;
    }

    private static AdminVerification Admin() => new(AdminAccess.Granted, "admin-1");

    [Fact]
    public async Task GetDashboard_WithoutToken_ReturnsUnauthorized()
    {
        AdminController controller = CreateController(
            new AdminVerification(AdminAccess.InvalidToken, null),
            authorization: null);

        ActionResult result = await controller.GetDashboard(CancellationToken.None);

        Assert.IsType<UnauthorizedObjectResult>(result);
    }

    [Fact]
    public async Task GetDashboard_AsNonAdmin_ReturnsForbiddenAndSkipsTheService()
    {
        AdminController controller = CreateController(
            new AdminVerification(AdminAccess.NotAdmin, "student-1"));

        ActionResult result = await controller.GetDashboard(CancellationToken.None);

        ObjectResult forbidden = Assert.IsType<ObjectResult>(result);
        Assert.Equal(StatusCodes.Status403Forbidden, forbidden.StatusCode);
        _query.Verify(
            service => service.GetDashboardAsync(It.IsAny<CancellationToken>()),
            Times.Never);
    }

    [Fact]
    public async Task GetDashboard_AsAdmin_ReturnsTheDashboard()
    {
        var dashboard = new AdminDashboardResponse(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, []);
        _query
            .Setup(service => service.GetDashboardAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(dashboard);
        AdminController controller = CreateController(Admin());

        ActionResult result = await controller.GetDashboard(CancellationToken.None);

        OkObjectResult ok = Assert.IsType<OkObjectResult>(result);
        Assert.Same(dashboard, ok.Value);
        _verifier.Verify(verifier => verifier.VerifyAsync("token"), Times.Once);
    }

    [Fact]
    public async Task RejectListing_PassesTheAdminIdAndReason()
    {
        _actions
            .Setup(service => service.RejectListingAsync(
                "admin-1", "a1", "Add photos", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new AdminActionResponse("ListingRejected", "Accommodation", "a1", "Listing rejected."));
        AdminController controller = CreateController(Admin());

        ActionResult result = await controller.RejectListing(
            "a1",
            new AdminReasonRequest { Reason = "Add photos" },
            CancellationToken.None);

        Assert.IsType<OkObjectResult>(result);
        _actions.Verify(
            service => service.RejectListingAsync(
                "admin-1", "a1", "Add photos", It.IsAny<CancellationToken>()),
            Times.Once);
    }

    [Fact]
    public async Task RejectListing_WhenListingIsMissing_ReturnsNotFound()
    {
        _actions
            .Setup(service => service.RejectListingAsync(
                It.IsAny<string>(), It.IsAny<string>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new KeyNotFoundException("The accommodation was not found."));
        AdminController controller = CreateController(Admin());

        ActionResult result = await controller.RejectListing(
            "missing",
            new AdminReasonRequest { Reason = "Reason" },
            CancellationToken.None);

        Assert.IsType<NotFoundObjectResult>(result);
    }

    [Fact]
    public async Task ApproveListing_WhenStateIsWrong_ReturnsConflict()
    {
        _actions
            .Setup(service => service.ApproveListingAsync(
                It.IsAny<string>(), It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new InvalidOperationException("Only a pending listing can be approved."));
        AdminController controller = CreateController(Admin());

        ActionResult result = await controller.ApproveListing("a1", CancellationToken.None);

        Assert.IsType<ConflictObjectResult>(result);
    }

    [Fact]
    public async Task RejectListing_WithoutReason_ReturnsBadRequest()
    {
        _actions
            .Setup(service => service.RejectListingAsync(
                It.IsAny<string>(), It.IsAny<string>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new ArgumentException("A reason is required."));
        AdminController controller = CreateController(Admin());

        ActionResult result = await controller.RejectListing("a1", null, CancellationToken.None);

        Assert.IsType<BadRequestObjectResult>(result);
    }

    [Fact]
    public async Task GetListing_WhenMissing_ReturnsNotFound()
    {
        _query
            .Setup(service => service.GetListingAsync("missing", It.IsAny<CancellationToken>()))
            .ReturnsAsync((AdminListingDetailResponse?)null);
        AdminController controller = CreateController(Admin());

        ActionResult result = await controller.GetListing("missing", CancellationToken.None);

        Assert.IsType<NotFoundObjectResult>(result);
    }

    [Fact]
    public async Task GetUsers_PassesTheFilters()
    {
        _query
            .Setup(service => service.GetUsersAsync("Manager", "dan", It.IsAny<CancellationToken>()))
            .ReturnsAsync(Array.Empty<AdminUserSummary>());
        AdminController controller = CreateController(Admin());

        ActionResult result = await controller.GetUsers("Manager", "dan", CancellationToken.None);

        Assert.IsType<OkObjectResult>(result);
        _query.Verify(
            service => service.GetUsersAsync("Manager", "dan", It.IsAny<CancellationToken>()),
            Times.Once);
    }
}