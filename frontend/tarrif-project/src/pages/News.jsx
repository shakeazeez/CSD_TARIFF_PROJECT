import { useState, useEffect } from "react";
import { Header } from "../components/Header.jsx";
import axios from "axios";

export function News({ onMenuClick }) {

    const [newsData, setNewsData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedNews, setSelectedNews] = useState(null); // <-- modal state

    let testingData = [
        { id: 1, title: "News 1", content: "This is content for news 1.", date: "2025-10-01" },
        { id: 2, title: "News 2", content: "This is content for news 2.", date: "2025-10-02" }
    ];

    useEffect(() => {
        const fetchNews = async () => {
            try {
                const response = await axios.get("https://api.example.com/news");
                // ensure we store an array; fallback to testingData if unexpected shape
                const data = Array.isArray(response.data) ? response.data : testingData;
                setNewsData(data);
            } catch (error) {
                console.error("Error fetching news data:", error);
                setNewsData(testingData); // Fallback to testing data on error
            } finally {
                setLoading(false);
            }
        };
        fetchNews();
    }, []);

    const list = Array.isArray(newsData) ? newsData : testingData;

    // lock body scroll while modal is open and add ESC key handler
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
        <>
        {/* TOP NAVIGATION */}
        <Header onMenuClick={onMenuClick} showUserInfo={true} />
        <div>
            {loading ? (
                <div className="min-h-[200px] flex items-center justify-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900" />
                </div>
            ) : (
                <div className="space-y-6">
                    {list.map((news) => (
                        <section
                            key={news.id ?? news.title}
                            className="bg-white dark:bg-gray-800 rounded-lg shadow p-6"
                            role="article"
                            aria-labelledby={`news-${news.id}`}
                        >
                            <header className="mb-3">
                                <h2 id={`news-${news.id}`} className="text-xl md:text-2xl font-semibold">
                                    {news.title}
                                </h2>
                                {news.date && (
                                    <p className="text-sm text-gray-500 mt-1">
                                        {new Date(news.date).toLocaleDateString()}
                                    </p>
                                )}
                            </header>

                            <div className="prose dark:prose-invert max-w-none text-gray-700">
                                <p>{news.content}</p>
                            </div>

                            {/* optional footer for actions/metadata */}
                            <footer className="mt-4 text-sm text-gray-500 flex justify-between">
                                <span>{news.source ?? "Unknown source"}</span>
                                <button
                                    className="text-sm text-blue-600 hover:underline"
                                    onClick={() => setSelectedNews(news)}
                                    aria-haspopup="dialog"
                                    aria-controls={news.id ? `dialog-${news.id}` : undefined}
                                >
                                    Read more
                                </button>
                            </footer>
                        </section>
                    ))}
                </div>
            )}
        </div>

        {/* Modal overlay */}
        {selectedNews && (
            <div
                className="fixed inset-0 z-50 flex items-center justify-center"
                role="dialog"
                aria-modal="true"
                aria-labelledby={`dialog-title-${selectedNews.id ?? "news"}`}
            >
                {/* subtle spotlight backdrop (not a full blackout) */}
                <div
                    className="absolute inset-0"
                    onClick={() => setSelectedNews(null)}
                    style={{
                        // radial gradient creates a "spotlight" feel while keeping page visible
                        background:
                            "radial-gradient(circle at 50% 40%, rgba(0,0,0,0.06) 0%, rgba(0,0,0,0.12) 35%, rgba(0,0,0,0.18) 100%)",
                        backdropFilter: "blur(4px)" // subtle blur to focus on dialog
                    }}
                />

                {/* dialog */}
                <div
                    id={`dialog-${selectedNews.id ?? "news"}`}
                    className="relative max-w-3xl w-full mx-4 bg-white dark:bg-gray-900 rounded-lg shadow-2xl ring-1 ring-black/10 p-6 z-10 overflow-auto max-h-[90vh]"
                >
                    <div className="flex items-start justify-between">
                        <h3 id={`dialog-title-${selectedNews.id ?? "news"}`} className="text-2xl font-semibold">
                            {selectedNews.title}
                        </h3>
                        <button
                            aria-label="Close"
                            className="text-gray-600 hover:text-gray-900 ml-4"
                            onClick={() => setSelectedNews(null)}
                        >
                            ✕
                        </button>
                    </div>

                    {selectedNews.date && (
                        <p className="text-sm text-gray-500 mt-2">
                            {new Date(selectedNews.date).toLocaleString()}
                        </p>
                    )}

                    <div className="prose dark:prose-invert mt-4 text-gray-700">
                        <p>{selectedNews.content}</p>
                    </div>

                    {selectedNews.source && (
                        <div className="mt-6 text-sm text-gray-500">Source: {selectedNews.source}</div>
                    )}
                </div>
            </div>
        )}
        </>
    );
}