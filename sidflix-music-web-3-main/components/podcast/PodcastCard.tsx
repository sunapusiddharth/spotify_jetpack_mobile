import * as React from "react";
import CardContent from '@mui/material/CardContent';
import Typography from '@mui/material/Typography';
import { Card } from '@mui/material'
import { PodcastCardDto } from "../../types/PodcastCard.dto";
import Link from "next/link";
import Image from "next/image";

interface CompProps {
    station: PodcastCardDto
}

function CardComp({ station }: CompProps) {
    return <Link href={`/podcast/${station.uuid}`}><Card className='podcast_carousel_card'
        sx={{
            // width: 200,
            width: { xs: 150, md: 200 },
            height: 230, display: 'flex',
            flexDirection: 'column',
            alignContent: 'center',
            alignItems: 'center',
            justifyContent: ' space-around',
            '&:hover': {
                cursor: 'pointer'
            },
            background: 'unset'
        }}>
        <Image src={station.image_url || "/sample.jpg"}  className='image' 
        width={200} height={200}
         alt="" />
        <CardContent sx={{ fontSize: 'small', textAlign: 'center', padding: 0, paddingBottom: '3px !important' }}>
            {station.title != station.itunes_author ? <Typography fontSize={"x-small"} color="text.secondary">{station.itunes_author} </Typography> : <></>}
            <Typography fontSize={"small"}>
                {station.title}
            </Typography>
        </CardContent>
    </Card >
    </Link>
}

function moviePropsAreEqual(
    prevProps: CompProps,
    nextProps: CompProps
) {
    return prevProps === nextProps;
}

export const PodcastCard = React.memo(
    CardComp,
    moviePropsAreEqual
);

export type PodcastsPageInfo = {
    results: PodcastCardDto[],
    page: number
}