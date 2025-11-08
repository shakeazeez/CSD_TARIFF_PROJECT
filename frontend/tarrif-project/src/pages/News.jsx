import { useState, useEffect, useMemo } from "react";
import { Header } from "../components/Header.jsx";
import axios from "axios";
import { useTheme } from "../contexts/use-theme.js";
import { Newspaper, ExternalLink, Calendar, X } from "lucide-react";

export function News({ onMenuClick }) {
    const backendURL = import.meta.env.VITE_BACKEND_URL || '';
    const { colors } = useTheme();
    const [newsData, setNewsData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedNews, setSelectedNews] = useState(null);

    const testingData = useMemo(() => [
        { id: 1, title: "News 1", content: "This is content for news 1.", date: "2025-10-01" },
        { id: 2, title: "News 2", content: "This is content for news 2.", date: "2025-10-02" }
    ], []);

    useEffect(() => {
        const fetchNews = async () => {
            try {
                const response = await axios.get(`${backendURL}/news/articles`);
                setNewsData(response.data);
            } catch (error) {
                console.error("Error fetching news data:", error);
                setNewsData(testingData);
            } finally {
                setLoading(false);
            }
        };
        fetchNews();
    }, [backendURL, testingData]);

    const list = Array.isArray(newsData) ? newsData : testingData;

    useEffect(() => {
        if (!selectedNews) return;
        const prev = document.body.style.overflow;
        document.body.style.overflow = "hidden";

        const onKey = (e) => {
            if (e.key === "Escape") setSelectedNews(null);
        };
        window.addEventListener("keydown", onKey);
        return () => {
            window.removeEventListener("keydown", onKey);
            document.body.style.overflow = prev;
        };
    }, [selectedNews]);

    return (
        <div className="min-h-screen" style={{ background: 'transparent' }}>
            <Header onMenuClick={onMenuClick} showUserInfo={true} />

            <div className="max-w-6xl mx-auto px-4 py-8">
                {/* Page Header */}
                <div className="text-center mb-8">
                    <div className="flex items-center justify-center mb-4">
                        <Newspaper className="h-8 w-8 mr-3" style={{ color: colors.accent }} />
                        <h1 className="text-3xl md:text-4xl font-bold" style={{ color: colors.foreground }}>
                            Latest News & Articles
                        </h1>
                    </div>
                    <p className="text-lg" style={{ color: colors.muted }}>
                        Stay updated with the latest tariff news and market insights
                    </p>
                </div>

                {loading ? (
                    <div className="flex items-center justify-center py-20">
                        <div className="animate-spin rounded-full h-16 w-16 border-b-2" style={{ borderColor: colors.accent }} />
                    </div>
                ) : (
                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                        {list.map((news) => (
                            <article
                                key={news.id ?? news.title}
                                className="rounded-xl shadow-lg hover:shadow-xl transition-all duration-300 transform hover:-translate-y-1"
                                style={{
                                    backgroundColor: colors.surface,
                                    border: `1px solid ${colors.border}`
                                }}
                                role="article"
                                aria-labelledby={`news-${news.id}`}
                            >
                                <div className="p-6">
                                    <header className="mb-4">
                                        <h2
                                            id={`news-${news.id}`}
                                            className="text-xl font-semibold mb-2 line-clamp-2"
                                            style={{ color: colors.foreground }}
                                        >
                                            {news.title}
                                        </h2>
                                        {news.date && (
                                            <div className="flex items-center text-sm" style={{ color: colors.muted }}>
                                                <Calendar className="h-4 w-4 mr-1" />
                                                {new Date(news.date).toLocaleDateString('en-US', {
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric'
                                                })}
                                            </div>
                                        )}
                                    </header>

                                    <div className="mb-4">
                                        <p
                                            className="text-sm line-clamp-3 leading-relaxed"
                                            style={{ color: colors.foreground }}
                                        >
                                            {news.content}
                                        </p>
                                    </div>

                                    <footer className="flex items-center justify-between pt-4 border-t" style={{ borderColor: colors.border }}>
                                        {news.url && (
                                            <a
                                                href={news.url}
                                                target="_blank"
                                                rel="noopener noreferrer"
                                                className="flex items-center text-sm font-medium hover:underline transition-colors"
                                                style={{ color: colors.accent }}
                                            >
                                                <ExternalLink className="h-4 w-4 mr-1" />
                                                Read Full Article
                                            </a>
                                        )}
                                        <button
                                            className="px-4 py-2 rounded-lg font-medium transition-all duration-200 hover:scale-105"
                                            style={{
                                                backgroundColor: colors.accent,
                                                color: '#ffffff'
                                            }}
                                            onClick={() => setSelectedNews(news)}
                                            aria-haspopup="dialog"
                                            aria-controls={news.id ? `dialog-${news.id}` : undefined}
                                        >
                                            Read More
                                        </button>
                                    </footer>
                                </div>
                            </article>
                        ))}
                    </div>
                )}

                {/* Modal */}
                {selectedNews && (
                    <div
                        className="fixed inset-0 z-50 flex items-center justify-center p-4"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby={`dialog-title-${selectedNews.id ?? "news"}`}
                    >
                        <div
                            className="absolute inset-0"
                            onClick={() => setSelectedNews(null)}
                            style={{
                                background: "radial-gradient(circle at 50% 40%, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.2) 35%, rgba(0,0,0,0.3) 100%)",
                                backdropFilter: "blur(8px)"
                            }}
                        />

                        <div
                            id={`dialog-${selectedNews.id ?? "news"}`}
                            className="relative max-w-2xl w-full max-h-[90vh] rounded-xl shadow-2xl overflow-hidden"
                            style={{
                                backgroundColor: colors.surface,
                                border: `1px solid ${colors.border}`
                            }}
                        >
                            <div className="p-6 md:p-8">
                                <div className="flex items-start justify-between mb-6">
                                    <div className="flex-1">
                                        <h3
                                            id={`dialog-title-${selectedNews.id ?? "news"}`}
                                            className="text-2xl md:text-3xl font-bold mb-2"
                                            style={{ color: colors.foreground }}
                                        >
                                            {selectedNews.title}
                                        </h3>
                                        {selectedNews.date && (
                                            <div className="flex items-center text-sm" style={{ color: colors.muted }}>
                                                <Calendar className="h-4 w-4 mr-2" />
                                                {new Date(selectedNews.date).toLocaleDateString('en-US', {
                                                    year: 'numeric',
                                                    month: 'long',
                                                    day: 'numeric'
                                                })}
                                            </div>
                                        )}
                                    </div>
                                    <button
                                        aria-label="Close"
                                        className="p-2 rounded-full hover:bg-opacity-20 transition-colors"
                                        style={{ color: colors.muted }}
                                        onClick={() => setSelectedNews(null)}
                                    >
                                        <X className="h-6 w-6" />
                                    </button>
                                </div>

                                <div className="prose prose-lg max-w-none mb-6">
                                    <p
                                        className="leading-relaxed text-base md:text-lg"
                                        style={{ color: colors.foreground }}
                                    >
                                        {selectedNews.content}
                                    </p>
                                </div>

                                {selectedNews.url && (
                                    <div className="flex justify-end pt-4 border-t" style={{ borderColor: colors.border }}>
                                        <a
                                            href={selectedNews.url}
                                            target="_blank"
                                            rel="noopener noreferrer"
                                            className="inline-flex items-center px-6 py-3 rounded-lg font-medium transition-all duration-200 hover:scale-105"
                                            style={{
                                                backgroundColor: colors.accent,
                                                color: '#ffffff'
                                            }}
                                        >
                                            <ExternalLink className="h-4 w-4 mr-2" />
                                            Read Full Article
                                        </a>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}