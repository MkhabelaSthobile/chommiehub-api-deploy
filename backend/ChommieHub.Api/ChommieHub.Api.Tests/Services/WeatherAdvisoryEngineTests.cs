using ChommieHub.Api.Services;

namespace ChommieHub.Api.Tests.Services;

/// <summary>
/// Verifies weather-aware residence maintenance decisions without calling
/// the external Open-Meteo service.
/// </summary>
public sealed class WeatherAdvisoryEngineTests
{
    [Fact]
    public void DescribeWeather_ClearCode_ReturnsClear()
    {
        string result = WeatherAdvisoryEngine.DescribeWeather(0);

        Assert.Equal("Clear", result);
    }

    [Fact]
    public void DescribeWeather_RainCode_ReturnsRain()
    {
        string result = WeatherAdvisoryEngine.DescribeWeather(63);

        Assert.Equal("Rain", result);
    }

    [Fact]
    public void BuildMaintenanceAdvisory_Rain_RecommendsReschedulingOutdoorWork()
    {
        string result = WeatherAdvisoryEngine.BuildMaintenanceAdvisory(4.5, 10, 63);

        Assert.Contains("Wet weather", result);
        Assert.Contains("rescheduling", result);
    }

    [Fact]
    public void BuildMaintenanceAdvisory_StrongWind_ReturnsSafetyWarning()
    {
        string result = WeatherAdvisoryEngine.BuildMaintenanceAdvisory(0, 42, 1);

        Assert.Contains("Strong wind", result);
        Assert.Contains("safety", result);
    }

    [Fact]
    public void BuildMaintenanceAdvisory_ClearConditions_AllowsRoutineOutdoorMaintenance()
    {
        string result = WeatherAdvisoryEngine.BuildMaintenanceAdvisory(0, 12, 1);

        Assert.Equal("Current weather presents no obvious restriction for routine outdoor maintenance.", result);
    }
}