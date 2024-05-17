import React from 'react'


interface Props {
    trend: any,
}
export const Trend = ({ trend }: Props) => {

    return (
        <div style={{
            display: "flex",
            flexDirection: "row",
            gap: "10px",
            fontWeight: "500 !important",
            cursor: "pointer",

        }}>
            <span>{trend.name}
            </span>
            <span>
                {trend.difference >= 0 ? <span style={{ color: "green" }} title="Increase">▲</span> : <span style={{ color: "red" }} title="Decrease">▼</span>}
                <span > {Math.round(Math.abs(trend.difference))} %</span>
            </span>
        </div>
    )
}
