
export type PodcastDto = {
    id: string,
    image: string,
    title: string,
    genre_ids: number[],
    publisher: string,
    total_episodes: number,
    description: string
    episodes: { id: string, name: string, summary: string, thumbnail: string, duration: number }[]
}
