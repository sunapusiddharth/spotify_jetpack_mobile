import React, { useCallback, useContext, useEffect, useState } from "react"
import { Box, Grid, Typography } from "@mui/material"
import Image from 'next/image'
import { AppContext, AppContextType } from "../../pages/_app"
import { searchAll, fetchRadio, getSong } from "../../api"
import { ExpandMore } from "@mui/icons-material"
import { SearchPageResType, SearchTypeEnum } from "../../types/Song.type"
import { getImageUrl } from "../../utils"
import { useRouter } from "next/router"
import { AlbumsSearch } from "./AlbumsSearch"
import { Artists } from "./Artists"

const Albums = ({ type }: { type: SearchTypeEnum }) => {

    const [movies, setMovies] = useState<SearchPageResType>()
    const [page, setPage] = useState<number>(1)
    const [totalPages, setTotalPages] = useState<number>(0)
    const [loading, setLoading] = useState<boolean>(false)
    const router = useRouter()
    const { searchTerm, user, setStation, setCurrentSong } = useContext(AppContext) as AppContextType
    const fetchData = useCallback(async () => {
        setLoading(true)
        return await searchAll(searchTerm, type, page).then(response => {
            setMovies(response)
            setTotalPages(Math.round(response?.total || 0 / 50 || 0))
            setLoading(false)
        })

    }, [searchTerm]);

    const loadMoreMovies = () => {
        setLoading(true)
        queryNextBatch(page)
        setLoading(false)
    }

    const queryNextBatch = async (page: number) => {
        let nextPage = page + 1

        return await searchAll(searchTerm, type, nextPage).then(response => {
            if (movies) {
                setMovies(response)
                setPage(page + 1)
            }
        })
    }

    useEffect(() => {
        fetchData()
        return () => setMovies(undefined)
    }, [fetchData])

    async function onClicHandler(id: string, type: SearchTypeEnum) {
        if (type == SearchTypeEnum.podcast) {
            router.push(`/podcast/${id}`)
        }
        if (type == SearchTypeEnum.artist) {
            router.push(`/artist/${id}`)
        }
        if (type == SearchTypeEnum.album) {
            router.push(`/album/${id}`)
        }
        if (type == SearchTypeEnum.radio) {
            const radio = await fetchRadio(id)
            setStation(radio)
        }
        if (type == SearchTypeEnum.songs) {
            const song = await getSong(id)
            setCurrentSong(song)
        }
    }

    // const spotifySearch = async () => {
    //     if (!user) return null;
    //     await requestSpotifysearch(user.id, searchTerm, 1);
    // }
    if (!movies?.cards?.length) {
        if (type == SearchTypeEnum.album) {
            return <Box sx={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
                <Typography>Sorry no results found.</Typography>
                <AlbumsSearch />
            </Box>
        }
        if (type == SearchTypeEnum.artist) {
            return <Box sx={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
                <Typography>Sorry no results found.</Typography>
                <Artists />
            </Box>
        }
        return <Box sx={{display:'flex',justifyContent:'space-between',alignItems:'center',height:'100%',width:'100%'}}>
            <Typography>Sorry no results found.</Typography>
        </Box>
    }
    return (
        <Box mt={2}>
            <Grid container marginTop='3%' display='flex' columns={{ sx: 2, sm: 5, md: 5, lg: 7 }} columnGap='2%' rowGap='5%' width='100%' justifyContent='center'>
                {movies?.cards?.map((x, index) => <Box key={'track' + index + x.id} sx={{ textAlign: 'center', margin: '1%' }} width={150} height={170}
                    onClick={() => onClicHandler(x.id, x.type)} >
                    <Image alt="" src={getImageUrl(x.image) || "/sample.jpg"} width={150} height={150} />
                    <Typography fontSize={"small"}>{x.name}</Typography>
                </Box>
                )}
            </Grid>
            {page < totalPages && <div className="load-more" onClick={() => loadMoreMovies()}>
                <span>
                    <ExpandMore />
                </span>
            </div>
            }
        </Box>

    )
}


export default Albums