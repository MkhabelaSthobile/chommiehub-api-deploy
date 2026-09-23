using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.DependencyInjection;
using GrpcStatusCode = Grpc.Core.StatusCode;
using RpcException = Grpc.Core.RpcException;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides health checks for the ChommieHub REST API and its Firestore connection.
    /// The standard endpoint checks whether the API is running, while the Firestore endpoint verifies that the API can authenticate to and communicate with the configured database.
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public sealed class HealthController : ControllerBase
    {
        private readonly IServiceProvider _services;
        private readonly ILogger<HealthController> _logger;

        public HealthController(IServiceProvider services, ILogger<HealthController> logger)
        {
            _services = services;
            _logger = logger;
        }

        /// <summary>
        /// Confirms that the ASP.NET Core API process is running.
        /// This check does not require Firestore access.
        /// </summary>
        [HttpGet]
        public IActionResult GetHealth()
        {
            DateTime timestampUtc = DateTime.UtcNow;

            _logger.LogInformation("ChommieHub API health check requested at {TimeUtc}.", timestampUtc);

            return Ok(new
            {
                status = "Healthy",
                service = "ChommieHub.Api",
                environment = Environment.GetEnvironmentVariable("ASPNETCORE_ENVIRONMENT") ?? "Production",
                timestampUtc
            });
        }

        /// <summary>
        /// Confirms that the API can authenticate to and query the configured Firestore project.
        /// The health check performs a read-only query and does not create or modify application data.
        /// </summary>
        [HttpGet("firestore")]
        public async Task<IActionResult> GetFirestoreHealth(CancellationToken cancellationToken)
        {
            try
            {
                FirestoreDb firestore = _services.GetRequiredService<FirestoreDb>();

                await firestore.Collection("_health").Limit(1).GetSnapshotAsync(cancellationToken);

                _logger.LogInformation("Firestore health check succeeded for project {ProjectId}.", firestore.ProjectId);

                return Ok(new
                {
                    status = "Healthy",
                    service = "Google Cloud Firestore",
                    projectId = firestore.ProjectId,
                    timestampUtc = DateTime.UtcNow
                });
            }
            catch (RpcException exception) when (exception.StatusCode is GrpcStatusCode.PermissionDenied or GrpcStatusCode.Unauthenticated)
            {
                _logger.LogWarning(exception, "Firestore authentication or IAM permission check failed.");

                return StatusCode(StatusCodes.Status503ServiceUnavailable, new
                {
                    status = "Unhealthy",
                    service = "Google Cloud Firestore",
                    reason = "Authentication or IAM permission failure.",
                    timestampUtc = DateTime.UtcNow
                });
            }
            catch (RpcException exception) when (exception.StatusCode is GrpcStatusCode.Unavailable or GrpcStatusCode.DeadlineExceeded)
            {
                _logger.LogWarning(exception, "Firestore is currently unreachable.");

                return StatusCode(StatusCodes.Status503ServiceUnavailable, new
                {
                    status = "Unhealthy",
                    service = "Google Cloud Firestore",
                    reason = "Firestore is currently unreachable.",
                    timestampUtc = DateTime.UtcNow
                });
            }
            catch (Exception exception)
            {
                _logger.LogWarning(exception, "Firestore client could not be initialised.");

                return StatusCode(StatusCodes.Status503ServiceUnavailable, new
                {
                    status = "Unhealthy",
                    service = "Google Cloud Firestore",
                    reason = "Firestore credentials or project configuration are unavailable.",
                    timestampUtc = DateTime.UtcNow
                });
            }
        }
    }
}