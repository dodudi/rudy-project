import {notFound} from 'next/navigation';
import {prisma} from '@/lib/db';
import {Post, Category} from '@/types';
import PostDetailWrapper from './PostDetailWrapper';

export const dynamic = 'force-dynamic';

export default async function PostPage({
    params,
}: {
    params: Promise<{id: string}>;
}) {
    const {id} = await params;

    const [dbPost, dbCategories] = await Promise.all([
        prisma.post.findUnique({where: {id}}),
        prisma.category.findMany({orderBy: {createdAt: 'asc'}}),
    ]);

    if (!dbPost) notFound();

    const post: Post = {
        ...dbPost,
        image: dbPost.image ?? null,
        createdAt: dbPost.createdAt.toISOString(),
        updatedAt: dbPost.updatedAt.toISOString(),
    };

    const categories: Category[] = dbCategories.map((c) => ({
        ...c,
        createdAt: c.createdAt.toISOString(),
    }));

    return <PostDetailWrapper post={post} categories={categories}/>;
}
