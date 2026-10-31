namespace ChommieHub.Api.Services.Interfaces
{
    public interface IFirebaseAccountAdmin
    {
        Task SetDisabledAsync(string uid, bool disabled, CancellationToken cancellationToken = default);

        Task DeleteAsync(string uid, CancellationToken cancellationToken = default);
    }
}