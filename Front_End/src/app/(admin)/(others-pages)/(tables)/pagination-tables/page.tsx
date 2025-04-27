"use client";
import ComponentCard from "@/components/common/ComponentCard";
import PageBreadcrumb from "@/components/common/PageBreadCrumb";
import BasicTableOne from "@/components/tables/BasicTableOne";
import Pagination from "@/components/tables/Pagination";
import { Metadata } from "next";
import React from "react";


export default function paginationTables() {
  return (
    <div>
      <PageBreadcrumb pageTitle="Reservation List" />
      <div className="space-y-6">
        <ComponentCard title="Basic Table 1">
          <Pagination currentPage={1}
            totalPages={10}
            onPageChange={(page) => console.log("Go to page:", page)} />
        </ComponentCard>
      </div>
    </div>
  );
}
