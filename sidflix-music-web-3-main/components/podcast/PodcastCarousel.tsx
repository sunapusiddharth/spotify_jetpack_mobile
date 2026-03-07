import * as React from "react";
import Link from "next/link";
import { Box, Grid, Typography } from "@mui/material";
import { PodcastCard } from "./PodcastCard";
import { PodcastCardDto } from "../../types/PodcastCard.dto";
import Slider from "react-slick";
import { AppContext, AppContextType } from "../../pages/_app";

interface CompProps {
    data: { label: string, cards: PodcastCardDto[], },
    genreid: string
}


export function PodcastCarousel(props: CompProps) {
    const {carouseSettings} = React.useContext(AppContext) as AppContextType

    const cards = props.data.cards || []
    if (!cards.length) return null
    const allCardsInCarousel = cards?.map((x, index) =>
        <PodcastCard station={x} key={'podcast-carousel-item' + x.uuid} />
    )
    return (
        <Box sx={{ paddingTop: '1% !important' }}>
            <Grid container justifyContent={'space-between'}>
                <Grid item>
                    <Typography variant='subtitle2'  >{props.data.label}</Typography>
                </Grid>
                {props.genreid ? <Grid item>
                    <Link href={`/podcast/genre/${props.genreid}`}>
                        <Typography fontSize={'small'} color='red' sx={{
                            ':hover': {
                                cursor: 'pointer',
                                color: 'red !important'
                            }
                        }}>See All</Typography>
                    </Link>
                </Grid> : ''}
            </Grid>
            <Box className='container' sx={{ paddingTop: '1%' }} >
                <Slider {...carouseSettings}>
                    {allCardsInCarousel}
                </Slider>
            </Box>
        </Box>

    )
}

