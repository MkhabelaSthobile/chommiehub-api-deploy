namespace ChommieHub.Api.Services
{
    /// <summary>
    /// Converts external weather codes and measurements into information useful
    /// for ChommieHub residence maintenance planning.
    ///
    /// The logic is separated from the external weather API so it can be unit tested
    /// without making live network requests.
    /// </summary>
    public static class WeatherAdvisoryEngine
    {
        public static string DescribeWeather(int weatherCode)
        {
            return weatherCode switch
            {
                0 => "Clear",
                1 or 2 => "Mostly clear",
                3 => "Overcast",
                45 or 48 => "Fog",
                >= 51 and <= 57 => "Drizzle",
                >= 61 and <= 67 => "Rain",
                >= 71 and <= 77 => "Snow",
                >= 80 and <= 82 => "Rain showers",
                >= 85 and <= 86 => "Snow showers",
                >= 95 => "Thunderstorm",
                _ => "Current conditions"
            };
        }

        public static string BuildMaintenanceAdvisory(double precipitationMm, double windSpeedKph, int weatherCode)
        {
            if (precipitationMm > 0 || weatherCode is >= 51 and <= 82 || weatherCode >= 95)
            {
                return "Wet weather may affect outdoor maintenance. Consider indoor work or rescheduling exposed tasks.";
            }

            if (windSpeedKph >= 35)
            {
                return "Strong wind may affect ladder, roof or other elevated outdoor maintenance. Review safety conditions before proceeding.";
            }

            return "Current weather presents no obvious restriction for routine outdoor maintenance.";
        }
    }
}