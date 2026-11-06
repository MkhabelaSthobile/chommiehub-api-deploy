using Azure.Storage.Blobs;
using Azure.Storage.Blobs.Models;

namespace ChommieHub.Api.Storage;

/// <summary>
/// Private Azure Blob container for residence photos. The connection string is read
/// only by the API process from AzureBlob:ConnectionString / environment secrets.
/// No storage key or SAS token is ever returned to the Android client.
/// </summary>
public sealed class AzureAccommodationImageStore
{
    private readonly BlobContainerClient? _container;

    public AzureAccommodationImageStore(IConfiguration configuration)
    {
        string? connectionString = configuration["AzureBlob:ConnectionString"];
        if (!string.IsNullOrWhiteSpace(connectionString))
        {
            string containerName = configuration["AzureBlob:Container"] ?? "chommiehub-residences";
            _container = new BlobContainerClient(connectionString, containerName);
        }
    }

    public bool IsConfigured => _container is not null;

    public async Task UploadAsync(string key, Stream data, string contentType, CancellationToken ct)
    {
        BlobContainerClient container = _container
            ?? throw new InvalidOperationException("Azure Blob Storage is not configured on the API server.");
        await container.CreateIfNotExistsAsync(PublicAccessType.None, cancellationToken: ct);
        await container.GetBlobClient(key).UploadAsync(data,
            new BlobUploadOptions { HttpHeaders = new BlobHttpHeaders { ContentType = contentType } }, ct);
    }

    public async Task<(Stream Content, string ContentType)?> OpenReadAsync(string key, CancellationToken ct)
    {
        BlobContainerClient container = _container
            ?? throw new InvalidOperationException("Azure Blob Storage is not configured on the API server.");
        var blob = container.GetBlobClient(key);
        if (!(await blob.ExistsAsync(ct)).Value) return null;
        var result = await blob.DownloadStreamingAsync(cancellationToken: ct);
        return (result.Value.Content, result.Value.Details.ContentType ?? "application/octet-stream");
    }
}
