import * as React from "react";
import { Box, Card, Typography } from "@mui/material";
import Slider, { Settings } from "react-slick";
import Link from "next/link";
import { CardContentType } from "../../types/CardType";
import Image from 'next/image'
import useWindowDimensions from "../../hooks/windowDimensio";

interface CompProps {
    data: CardContentType[]
}
export function FeaturedCarousel2(props: CompProps) {
    let sliderSettings: Settings = {
        swipeToSlide: true,
        className: "center",
        centerMode: true,
        infinite: true,
        centerPadding: "60px",
        slidesToShow: 3,
        speed: 500,
        autoplay: true,
        responsive: [{
            breakpoint: 600,
            settings: {
                slidesToShow: 1,
                slidesToScroll: 1,
                initialSlide: 1
            }
        }, {

            breakpoint: 480,
            settings: {
                slidesToShow: 1,
                slidesToScroll: 1
            }
        }]
    }
    const { width } = useWindowDimensions()
    const isMobile = width <= 480
    if (isMobile) {
        sliderSettings = { ...sliderSettings, centerMode: false }
    }
    props.data
    return (
        <Box className="carousel-container" mb={2} style={{ marginTop: 40 }} >
            <Typography mb={2} pt={4}>Editor's Choice</Typography>
            <Slider {...sliderSettings} >
                {props.data?.map(x =>
                    <Link href={'/playlist/' + x.id} key={'carousel-fresh0rela-' + x.id} style={{ marginRight: 3 }}>
                        <Card
                            className='radio_carousel_card'
                            sx={{
                                // backgroundColor: 'black !important',
                                width: { xs: '100%', md: 350 },
                                height: 300,
                                // height:'100%',width:'100%',
                                display: 'flex',
                                flexDirection: 'column',
                                alignContent: 'center',
                                alignItems: 'center',
                                justifyContent: ' space-around',
                                '&:hover': {
                                    backgroundColor: '#282626',
                                    cursor: 'pointer'
                                },
                            }}>
                            <Box sx={{ position: 'relative', height: '100%', width: '100%' }}>
                                <Image src={x.image || "/sample.jpg"}
                                    fill={true}
                                    // width={530} height={400} 
                                    // layout="contained"
                                    alt="" />
                                <Box
                                    sx={{
                                        position: 'absolute',
                                        bottom: 0,
                                        left: 0,
                                        width: '100%',
                                        bgcolor: 'rgba(0, 0, 0, 0.54)',
                                        color: 'white',
                                        padding: '13px',
                                        fontSize: 'small'
                                    }}
                                >
                                    <Typography >{x.title}</Typography>
                                </Box>
                            </Box>
                        </Card >
                    </Link>
                )}

            </Slider>

        </Box>
    )
}