import * as React from "react";
import { ContentCard } from "./Card";
import Link from "next/link";
import { Box, Grid, Typography } from "@mui/material";
import { HomePageDataType } from "../../types/HomePageData.type";
import { ArtistCard } from "../artist/ArtistCard";
import { splitOnCaps } from "../../utils";
import Slider from "react-slick";
import { AppContext, AppContextType } from "../../pages/_app";

interface CompProps {
    data: HomePageDataType
}

function CardComp(props: CompProps) {
    const cards = props.data.cards || []
    const {carouseSettings} = React.useContext(AppContext) as AppContextType

    const path = props.data.path && props.data.path !=='' ? props.data.path:`/playlist/${props.data.id}`
    if (!cards.length) return null
    return (
        <Box>
            <Grid container justifyContent={'space-between'} >
                <Grid item sx={{ marginBottom: 1 }}>
                    <Typography variant='subtitle2'>{splitOnCaps(props.data.label)}</Typography>
                </Grid>
                <Grid item>
                    <Link href={path}>
                    See All
                    </Link>
                </Grid>
            </Grid>
            <Box className='container'>
                <Slider {...carouseSettings}>
                    {cards?.map((x, index) => {
                        if (x.type == 'artists_card') return <ArtistCard data={x} key={'1245carousel-item'+props.data.label + index} />
                        return <ContentCard data={x} key={'carousel-item'+props.data.label + index} />
                    })}
                </Slider>
            </Box>

        </Box>

    )
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    // console.log("Props comparison 2", prevProps, nextProps, prevProps.data.label == nextProps.data.label)
    return prevProps.data.label == nextProps.data.label
}

export const CardsListing = React.memo(
    CardComp,
    moviePropsAreEqual
);
