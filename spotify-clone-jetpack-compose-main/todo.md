async getUserLikes(
  userId: string,
  pagination: PaginationDto
): Promise<PaginatedResponse<LikedEntityResponse>> {
  const page = Math.max(1, Number(pagination?.page) || 1);
  const limit = Math.min(100, Math.max(1, Number(pagination?.limit) || 20));
  const skip = (page - 1) * limit;

  const [likes, total] = await this.likesRepo.findAndCount({
    where: { userId },
    order: { likedAt: 'DESC' },
    take: limit,
    skip
  });

  if (likes.length === 0) {
    return {
      items: [],
      total: 0,
      page,
      limit,
      hasMore: false
    };
  }

  const entityIds = likes.map((like) => like.entityId);
  const historyData = await this.getHistoryBatch(userId, entityIds);

  const items = likes.map((like) =>
    this.mapToLikedResponse(like, historyData.get(like.entityId))
  );

  return {
    items,
    total,
    page,
    limit,
    hasMore: skip + items.length < total
  };
}


Keep this helper as a safe no-op on empty ids in the same file, or update it to this if you want it explicit:


private async getHistoryBatch(
  userId: string,
  entityIds: string[]
): Promise<Map<string, UserHistoryEntity>> {
  if (!entityIds.length) {
    return new Map();
  }

  const history = await this.historyRepo.find({
    where: {
      userId,
      entityId: In(entityIds)
    }
  });

  return new Map(history.map((entry) => [entry.entityId, entry]));
}

Why this is the right fix for point 1:

It normalizes page and limit.
It returns an empty paginated payload when the user has no likes.
It never tries to enrich from history if there are no like rows.
It preserves the existing response shape the Android app expects.
Expected empty response after this chang


{
  "items": [],
  "total": 0,
  "page": 1,
  "limit": 20,
  "hasMore": false
}


