import { Box, Grid, Typography } from "@mui/material";
import { RadioStationCard } from "./RadioCard";
import { RadioStation } from "../../types/RadioStationType";
import Slider from "react-slick";
import { AppContext, AppContextType } from "../../pages/_app";
import { useContext } from "react";

interface CompProps {
    data: { label: string, data: RadioStation[] }
}

export function RadioCarousel(props: CompProps) {
    const { carouseSettings } = useContext(AppContext) as AppContextType

    const cards = props.data.data || []
    if (!cards.length) return null
    return (
        <Box sx={{  marginTop: '1%' }}>
            <Grid container justifyContent={'space-between'} sx={{  }}>
                <Grid item>
                    <Typography variant='subtitle2'>{props.data.label}</Typography>
                </Grid>
            </Grid>
            <Box className='container' marginTop='1%'>
                <Slider {...carouseSettings}>
                    {cards.map((x, index) => <RadioStationCard station={x} key={'station-card' + x.id + index} />)}
                </Slider>
            </Box>

        </Box>

    )
}

