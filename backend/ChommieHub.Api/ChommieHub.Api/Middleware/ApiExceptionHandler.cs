using Grpc.Core;
using Microsoft.AspNetCore.Diagnostics;
using Microsoft.AspNetCore.Mvc;

namespace ChommieHub.Api.Middleware
{
    /// <summary>
    /// Handles unhandled API exceptions and converts them into safe HTTP responses.
    /// This prevents infrastructure failures, such as Firestore authentication errors,from terminating requests without a useful response for the Android application.
    /// </summary>
    public sealed class ApiExceptionHandler : IExceptionHandler
    {
        private readonly ILogger<ApiExceptionHandler> _logger;

        public ApiExceptionHandler(ILogger<ApiExceptionHandler> logger)
        {
            _logger = logger;
        }

        public async ValueTask<bool> TryHandleAsync(HttpContext httpContext, Exception exception, CancellationToken cancellationToken)
        {
            ProblemDetails problem = exception switch
            {
                RpcException grpcException when grpcException.StatusCode is StatusCode.PermissionDenied or StatusCode.Unauthenticated => new ProblemDetails
                {
                    Status = StatusCodes.Status503ServiceUnavailable,
                    Title = "Database authentication is unavailable.",
                    Detail = "The API is running but cannot authenticate to Firestore. Check the Firebase service-account credentials and IAM permissions."
                },

                RpcException grpcException when grpcException.StatusCode is StatusCode.Unavailable or StatusCode.DeadlineExceeded => new ProblemDetails
                {
                    Status = StatusCodes.Status503ServiceUnavailable,
                    Title = "Database service is temporarily unavailable.",
                    Detail = "The API could not reach Firestore. Check network connectivity and Google Cloud service availability."
                },

                _ => new ProblemDetails
                {
                    Status = StatusCodes.Status500InternalServerError,
                    Title = "An unexpected server error occurred.",
                    Detail = "The request could not be completed. Check the API logs for the supplied trace identifier."
                }
            };

            _logger.LogError(exception, "API request failed. TraceId: {TraceId}", httpContext.TraceIdentifier);

            problem.Extensions["traceId"] = httpContext.TraceIdentifier;
            httpContext.Response.StatusCode = problem.Status ?? StatusCodes.Status500InternalServerError;

            await httpContext.Response.WriteAsJsonAsync(problem, cancellationToken);

            return true;
        }
    }
}