

export const API_BASE_URL = {

    CHARGING_STATION: `${process.env.NEXT_PUBLIC_URL}/charging-station`,
    OUTLET: `${process.env.NEXT_PUBLIC_URL}/api/outlets`,
    CONSUMER: `${process.env.NEXT_PUBLIC_URL}/api/consumers`,
    RESERVATION: `${process.env.NEXT_PUBLIC_URL}/api/reservations`,
    SESSION: `${process.env.NEXT_PUBLIC_URL}/api/v1/sessions`,
    OPERATOR: `${process.env.NEXT_PUBLIC_URL}/api/operators`,
    CHARGING_STATION_OUTLETS: (id) =>
        `${process.env.NEXT_PUBLIC_URL}/charging-station/ChargingOutlets/${id}`,

}