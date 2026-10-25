
using Google.Cloud.Firestore;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.DependencyInjection;
using GrpcStatusCode = Grpc.Core.StatusCode;
using RpcException = Grpc.Core.RpcException;

namespace ChommieHub.Api.Controllers
{
    /// <summary>
    /// Provides health checks for the ChommieHub REST API
    /// and its Firestore connection.
    /// </summary>
    [ApiController]
    [Route("api/[controller]")]
    public sealed class HealthController : ControllerBase
    {
        private readonly IServiceProvider _services;
        private readonly ILogger<HealthController> _logger;

        public HealthController(
            IServiceProvider services,
            ILogger<HealthController> logger)
        {
            _services = services;
            _logger = logger;
        }

        /// <summary>
        /// Confirms that the API process is running.
        /// Does not require Firestore access.
        /// </summary>
        [HttpGet]
        public IActionResult GetHealth()
        {
            _logger.LogInformation(
                "ChommieHub API health check requested."
            );

            return Ok(new
            {
                status = "Healthy",
                service = "ChommieHub.Api",
                timestampUtc = DateTime.UtcNow
            });
        }

        /// <summary>
        /// Checks Firestore connectivity using a read-only query.
        /// Does not expose project IDs or internal error details.
        /// </summary>
        [HttpGet("firestore")]
        public async Task<IActionResult> GetFirestoreHealth(
            CancellationToken cancellationToken)
        {
            try
            {
                FirestoreDb firestore =
                    _services.GetRequiredService<FirestoreDb>();

                await firestore
                    .Collection("_health")
                    .Limit(1)
                    .GetSnapshotAsync(cancellationToken);

                _logger.LogInformation(
                    "Firestore health check succeeded."
                );

                return Ok(new
                {
                    status = "Healthy",
                    service = "Google Cloud Firestore",
                    timestampUtc = DateTime.UtcNow
                });
            }
            catch (RpcException exception)
                when (
                    exception.StatusCode is
                        GrpcStatusCode.PermissionDenied
                        or GrpcStatusCode.Unauthenticated
                )
            {
                _logger.LogWarning(
                    exception,
                    "Firestore authentication or IAM check failed."
                );
            }
            catch (RpcException exception)
                when (
                    exception.StatusCode is
                        GrpcStatusCode.Unavailable
                        or GrpcStatusCode.DeadlineExceeded
                )
            {
                _logger.LogWarning(
                    exception,
                    "Firestore connectivity check failed."
                );
            }
            catch (OperationCanceledException)
                when (cancellationToken.IsCancellationRequested)
            {
                // Preserve request cancellation rather than
                // treating it as a Firestore failure.
                throw;
            }
            catch (Exception exception)
            {
                _logger.LogError(
                    exception,
                    "Firestore health check failed."
                );
            }

            // Do not disclose internal failure details to clients.
            return StatusCode(
                StatusCodes.Status503ServiceUnavailable,
                new
                {
                    status = "Unhealthy",
                    service = "Google Cloud Firestore",
                    timestampUtc = DateTime.UtcNow
                }
            );
        }
    }
}
