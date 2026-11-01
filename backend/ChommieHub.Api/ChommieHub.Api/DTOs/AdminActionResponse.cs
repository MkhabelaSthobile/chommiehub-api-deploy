namespace ChommieHub.Api.DTOs
{
    // The outcome of an administrator action.
    
    public sealed record AdminActionResponse(
        string Action,
        string TargetType,
        string TargetId,
        string Message);
}