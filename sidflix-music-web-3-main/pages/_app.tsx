import "../styles/globals.css";
import type { AppProps } from "next/app";
import { Hydrate, QueryClient, QueryClientProvider, useQueryClient } from "react-query";
import React, { useState } from "react";
import { ThemeProvider, createTheme } from "@mui/material/styles";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import { SongType } from "../types/Song.type";
import { Songcomponent } from "../components/SongComponent";
import { UserType } from "../types/User.type";
import { addToQueue, fetchUserData } from "../api";
import { SessionProvider, signIn, useSession } from "next-auth/react"
import SongAppBar from "../components/common/AppBar";
import { Box, Drawer, NoSsr, Toolbar } from "@mui/material";
import { SideBar } from "../components/common/SideBar";
import { ReactQueryDevtools } from 'react-query/devtools'
import { RadioStation } from "../types/RadioStationType";
import { PodcastEpisodeDto } from "../types/PodcastEpisode.dto";
import { UserPlayLists } from "../components/common/UserPlayListModal";
import { QueueComp } from "../components/Queue";
import useWindowDimensions from "../hooks/windowDimensio";
import { Settings } from "react-slick";
import { PodcastCardDto } from "../types/PodcastCard.dto";
const darkTheme = createTheme({
  palette: {
    mode: "dark",
  },
});
export type PodcastEpisodeDtoExtend = {
  episode: PodcastEpisodeDto,
  podcast: PodcastCardDto
}
export type AppContextType = {
  user: UserType | undefined,
  setUser: (user: UserType) => void;
  song: SongType | undefined
  setCurrentSong: (song: SongType) => void;
  songForAddToPlc: SongType | undefined
  setsongForAddToPlc: (song: SongType) => void;
  openSideBarMeu: boolean;
  toggleSideBarMenu: (toggle: boolean) => void;
  searchTerm: string;
  setSearchTerm: (term: string) => void;
  station: RadioStation | undefined,
  setStation: (station: RadioStation) => void;
  stationForAddToPlc: RadioStation | undefined
  setstationForAddToPlc: (song: RadioStation) => void;
  stations: RadioStation[] | undefined,
  setStations: (station: RadioStation[]) => void;
  setPodcast: (song: PodcastEpisodeDtoExtend) => void;
  podcast: PodcastEpisodeDtoExtend | undefined,
  podcastEpisodes: PodcastEpisodeDto[],
  setPodcastEpisodes: (song: PodcastEpisodeDto[]) => void,
  openAddToPlayListMenu: boolean;
  setopenAddToPlayListMenu: (toggle: boolean) => void;
  bgColor: React.CSSProperties | undefined;
  setBgColor: (bgColor: React.CSSProperties) => void;
  openQueue: boolean;
  setopenQueue: (toggle: boolean) => void;
  carouseSettings: Settings,

};
export const AppContext = React.createContext<AppContextType | null>(null);

export default function App({
  Component,
  pageProps: { session, ...pageProps },
}: AppProps) {
  let width = 1024
  if (typeof window !== 'undefined') {
    // detect window screen width function
    ({ width } = useWindowDimensions())
  }

  const [queryClient] = React.useState(() => new QueryClient({
    defaultOptions: {
      queries: {
        refetchOnWindowFocus: false,
        keepPreviousData: true,
        refetchOnMount: false,
        cacheTime: 36000
      },
    },
  }));
  const initSettings: Settings = {
    dots: false,
    infinite: false,
    arrows:false,
    speed: 500,
    slidesToShow: 6,
    slidesToScroll: 6,
    initialSlide: 0,
    responsive: [{
      breakpoint: 1024,
      settings: {
        slidesToShow: 3,
        slidesToScroll: 3,
        infinite: true,
        dots: true
      }
    },
    {
      breakpoint: 600,
      settings: {
        slidesToShow: 2,
        slidesToScroll: 2,
        initialSlide: 2
      }
    },
    {
      breakpoint: 480,
      settings: {
        slidesToShow: 2,
        slidesToScroll: 1
      }
    }]
  }
  const [bgColor, setBgColor] = useState<React.CSSProperties>({ background: '#1C0650!important' });
  const [carouseSettings, setCarouseSettings] = useState<Settings>({
    ...initSettings,
    slidesToShow: 7,
    slidesToScroll: 7
  });


  const [song, setCurrentSong] = useState<SongType>();
  const [songForAddToPlc, setsongForAddToPlc] = useState<SongType>();
  const [user, setUser] = useState<UserType>();
  const [station, setStation] = React.useState<RadioStation>()
  const [podcast, setPodcast] = React.useState<PodcastEpisodeDtoExtend>()
  const [stations, setStations] = React.useState<RadioStation[]>([])
  const [stationForAddToPlc, setstationForAddToPlc] = React.useState<RadioStation>()
  const [podcastEpisodes, setPodcastEpisodes] = React.useState<PodcastEpisodeDto[]>([])
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [openSideBarMeu, toggleSideBarMenu] = useState<boolean>(false);
  const [openQueue, setopenQueue] = React.useState(false);
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const [openAddToPlayListMenu, setopenAddToPlayListMenu] = React.useState(false);
  const setSong = async (song: SongType) => {
    if (user) await addToQueue(user.id, song.id)
    // console.log("setSong", song?.id)
    setCurrentSong(song);
  };
  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  React.useEffect(() => {
    // console.log("window width changed", width)
    if (width < 600) {
      setCarouseSettings({ ...initSettings, slidesToShow: 3, slidesToScroll: 3 })
    }
    if (width < 1024 && width > 600) {
      setCarouseSettings({ ...initSettings, slidesToShow: 4, slidesToScroll: 4 })
    }
    if (width >= 1024) {
      setCarouseSettings({ ...initSettings, slidesToShow: 6, slidesToScroll: 6 })
    }
  }, [width])


  // setInterval(() => {
  //   console.log("interval")
  //   // setBgColor(colorChanger())
  // }, 10000)

  if (typeof window === 'undefined') {
    return <></>;
  }
  const drawerWidth = 180;
  return (
    <NoSsr>
      <SessionProvider session={session}>

        <ThemeProvider theme={darkTheme}>
          <QueryClientProvider client={queryClient}>
            <Hydrate state={pageProps.dehydratedState}>
              <AppContext.Provider
                value={{
                  bgColor, setBgColor,
                  user,
                  setUser,
                  song,
                  setCurrentSong: setSong,
                  openSideBarMeu,
                  toggleSideBarMenu,
                  searchTerm,
                  setSearchTerm,
                  station,
                  setStation,
                  stations,
                  setStations,
                  podcast,
                  setPodcast,
                  podcastEpisodes,
                  setPodcastEpisodes,
                  openAddToPlayListMenu,
                  setopenAddToPlayListMenu,
                  songForAddToPlc,
                  setsongForAddToPlc,
                  openQueue, setopenQueue,
                  carouseSettings,
                  stationForAddToPlc, setstationForAddToPlc
                }}
              >
                <Auth>
                  <Box sx={{}}>
                    <Box sx={{ padding: { xs: 1, md: 0 } }}>
                      {/* <CssBaseline /> */}
                      <SongAppBar handleDrawerToggle={handleDrawerToggle} drawerWidth={drawerWidth} />
                      <Box
                        // component="nav"
                        sx={{
                          display: 'flex', width: { sm: drawerWidth }, flexShrink: { sm: 0 }, overflowY: 'scroll',
                          background: 'linear-gradient(0deg, #1c0650 0%, #4b0070 100%) !important',
                        }}
                        aria-label="mailbox folders"
                      >
                        {/* The implementation can be swapped with js to avoid SEO duplication of links. */}
                        <Drawer
                          // container={container}
                          variant="temporary"
                          open={mobileOpen}
                          onClose={handleDrawerToggle}
                          ModalProps={{
                            keepMounted: true, // Better open performance on mobile.
                          }}
                          sx={{
                            ...bgColor,
                            display: { xs: 'block', sm: 'none' },
                            '& .MuiDrawer-paper': {
                              boxSizing: 'border-box', width: drawerWidth + 50,
                            },
                          }}
                        >
                          <SideBar user={user} />
                        </Drawer>
                        <Drawer
                          variant="permanent"
                          sx={{
                            ...bgColor,
                            display: { xs: 'none', sm: 'block' },
                            '& .MuiDrawer-paper': {
                              boxSizing: 'border-box', width: drawerWidth, height: '89%', zIndex: 2,
                            },
                          }}
                          open
                        >
                          <SideBar user={user} />
                        </Drawer>
                      </Box>

                      <Box
                        component="main"
                        mt={1}
                        sx={{
                          marginLeft: { md: `${drawerWidth}px`, sm: 0 }, width: { md: `calc(100% - ${drawerWidth}px)`, sm: '100%' }, padding: '0px 8px', height: '90vh', display: 'flex', flexDirection: 'column',
                          background: 'linear-gradient(0deg, #1c0650 0%, #4b0070 100%) !important'
                          , color: 'white', overflowX: 'hidden'
                        }}
                      >
                        {/* <Toolbar className='toolbar' /> */}
                        <Component {...pageProps} />
                        <UserPlayLists key={songForAddToPlc?.id + 'user-playlists'} open={openAddToPlayListMenu} setOpen={setopenAddToPlayListMenu} song={songForAddToPlc} />
                      </Box>

                    </Box>
                    <Songcomponent drawerWidth={drawerWidth} />
                    <QueueComp />
                  </Box>
                </Auth>
              </AppContext.Provider>
            </Hydrate>
            <ReactQueryDevtools initialIsOpen={false} />
          </QueryClientProvider>
        </ThemeProvider>
      </SessionProvider>
    </NoSsr>
  );
}

function Auth({ children }: { children: React.ReactElement }) {
  const { data: session, status } = useSession({ required: true })
  const isUser = !!session?.user
  const { setUser, user } = React.useContext(
    AppContext
  ) as AppContextType;
  const queryClient = useQueryClient()

  React.useEffect(() => {
    if (status === 'loading') {
      // console.log("LOding now///")
      return
    } // Do nothing while loading
    if (!isUser) {
      // console.log("redirecting to singni now///")

      signIn()
    }
  }, [isUser, status])

  if (isUser && !user && session?.user?.id) {
    // console.log('useroresent', session)
    fetchUserData(session.user.id).then(data => {
      queryClient.setQueryData(['user', data.id], data)
      setUser(data)
    }).catch(error => console.error(error))
    return children
  }

  // Session is being fetched, or no user.
  // If no user, useEffect() will redirect.
  if (user) return children
  return <div>Loading...</div>
}
